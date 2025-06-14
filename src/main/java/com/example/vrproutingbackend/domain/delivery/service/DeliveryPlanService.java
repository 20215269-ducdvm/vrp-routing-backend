package com.example.vrproutingbackend.domain.delivery.service;

import com.example.vrproutingbackend.domain.delivery.dto.AssignDriverRequest;
import com.example.vrproutingbackend.domain.delivery.dto.CreateDeliveryPlanRequest;
import com.example.vrproutingbackend.domain.delivery.dto.DeliveryPlanResponse;
import com.example.vrproutingbackend.domain.delivery.dto.SendScheduleRequest;
import com.example.vrproutingbackend.domain.delivery.entity.DeliveryAssignment;
import com.example.vrproutingbackend.domain.delivery.entity.DeliveryPlan;
import com.example.vrproutingbackend.domain.delivery.repository.DeliveryAssignmentRepository;
import com.example.vrproutingbackend.domain.delivery.repository.DeliveryPlanRepository;
import com.example.vrproutingbackend.domain.routing.dto.VrpSolveRequest;
import com.example.vrproutingbackend.domain.routing.dto.VrpSolveResponse;
import com.example.vrproutingbackend.domain.routing.entity.RouteSolution;
import com.example.vrproutingbackend.domain.routing.repository.RouteSolutionRepository;
import com.example.vrproutingbackend.domain.routing.service.VrpSolverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryPlanService {

    @Autowired
    private DeliveryPlanRepository deliveryPlanRepository;

    @Autowired
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Autowired
    private VrpSolverService vrpSolverService;

    @Autowired
    private RouteSolutionRepository routeSolutionRepository;

    public DeliveryPlanResponse createDeliveryPlan(CreateDeliveryPlanRequest request) {
        try {
            // 1. Tạo delivery plan
            DeliveryPlan plan = new DeliveryPlan();
            plan.setId(UUID.randomUUID().toString());
            plan.setPlanName(request.getPlanName());
            plan.setDeliveryDate(request.getDeliveryDate());
            plan.setCoordinatorId(request.getCoordinatorId());
            plan.setStatus("DRAFT");
            plan.setTotalCustomers(request.getCustomers().size());
            plan.setTotalOrders(request.getCustomers().size());
            plan.setNotes(request.getNotes());
            plan.setCreatedAt(LocalDateTime.now());
            plan.setUpdatedAt(LocalDateTime.now());

            // 2. Lưu plan trước
            deliveryPlanRepository.save(plan);

            // 3. Tạo VRP problem và giải quyết
            VrpSolveRequest vrpRequest = convertToVrpRequest(request);
            VrpSolveResponse vrpResponse = vrpSolverService.solveProblem(vrpRequest);

            if ("SUCCESS".equals(vrpResponse.getStatus())) {
                // 4. Cập nhật plan với VRP problem ID
                // Tìm VRP problem vừa tạo (có thể cần thêm method trong VrpSolverService)
                plan.setStatus("OPTIMIZED");
                plan.setUpdatedAt(LocalDateTime.now());
                deliveryPlanRepository.save(plan);

                log.info("Created delivery plan {} with VRP solution", plan.getId());
            } else {
                plan.setStatus("FAILED");
                deliveryPlanRepository.save(plan);
                log.error("Failed to solve VRP for delivery plan {}: {}", plan.getId(), vrpResponse.getMessage());
            }

            return buildDeliveryPlanResponse(plan);

        } catch (Exception e) {
            log.error("Error creating delivery plan", e);
            throw new RuntimeException("Failed to create delivery plan: " + e.getMessage());
        }
    }

    public DeliveryPlanResponse assignDrivers(AssignDriverRequest request) {
        try {
            DeliveryPlan plan = deliveryPlanRepository.findById(request.getDeliveryPlanId())
                    .orElseThrow(() -> new RuntimeException("Delivery plan not found"));

            if (!"OPTIMIZED".equals(plan.getStatus())) {
                throw new RuntimeException("Cannot assign drivers to plan with status: " + plan.getStatus());
            }

            // Tìm route solution của plan này
            List<RouteSolution> solutions = routeSolutionRepository.findByVrpProblemIdOrderBySolvedAtDesc(plan.getVrpProblemId());
            if (solutions.isEmpty()) {
                throw new RuntimeException("No VRP solution found for this plan");
            }

            RouteSolution solution = solutions.get(0); // Lấy solution mới nhất

            // Parse solution data để lấy routes
            List<VrpSolveResponse.RouteData> routes = parseSolutionRoutes(solution.getSolutionData());

            // Tạo assignments
            for (AssignDriverRequest.DriverAssignment driverAssign : request.getAssignments()) {
                // Tìm route tương ứng với vehicle index
                VrpSolveResponse.RouteData route = routes.stream()
                        .filter(r -> r.getVehicleId() == driverAssign.getVehicleIndex())
                        .findFirst()
                        .orElse(null);

                if (route != null) {
                    DeliveryAssignment assignment = new DeliveryAssignment();
                    assignment.setId(UUID.randomUUID().toString());
                    assignment.setDeliveryPlanId(plan.getId());
                    assignment.setRouteSolutionId(solution.getId());
                    assignment.setVehicleIndex(driverAssign.getVehicleIndex());
                    assignment.setDriverId(driverAssign.getDriverId());
                    assignment.setDriverName(driverAssign.getDriverName());
                    assignment.setDriverPhone(driverAssign.getDriverPhone());
                    assignment.setCustomerSequence(convertToJsonArray(route.getCustomerSequence()));
                    assignment.setEstimatedDistance(route.getRouteDistance());
                    assignment.setEstimatedDuration(calculateDuration(route));
                    assignment.setStatus("ASSIGNED");
                    assignment.setAssignedAt(LocalDateTime.now());

                    deliveryAssignmentRepository.save(assignment);
                }
            }

            // Cập nhật status plan
            plan.setStatus("ASSIGNED");
            plan.setUpdatedAt(LocalDateTime.now());
            deliveryPlanRepository.save(plan);

            return buildDeliveryPlanResponse(plan);

        } catch (Exception e) {
            log.error("Error assigning drivers", e);
            throw new RuntimeException("Failed to assign drivers: " + e.getMessage());
        }
    }

    public String sendScheduleToDrivers(SendScheduleRequest request) {
        try {
            List<DeliveryAssignment> assignments = deliveryAssignmentRepository.findByDeliveryPlanIdOrderByVehicleIndex(request.getDeliveryPlanId());

            int sentCount = 0;
            for (DeliveryAssignment assignment : assignments) {
                if (request.getVehicleIndexes().contains(assignment.getVehicleIndex()) &&
                        "ASSIGNED".equals(assignment.getStatus())) {

                    // TODO: Implement actual notification to driver (SMS, Push notification, etc.)
                    // For now, just update status
                    assignment.setStatus("SENT");
                    assignment.setSentAt(LocalDateTime.now());
                    deliveryAssignmentRepository.save(assignment);

                    sentCount++;
                    log.info("Sent schedule to driver {} for vehicle {}", assignment.getDriverName(), assignment.getVehicleIndex());
                }
            }

            // Cập nhật plan status
            if (sentCount > 0) {
                DeliveryPlan plan = deliveryPlanRepository.findById(request.getDeliveryPlanId()).orElse(null);
                if (plan != null) {
                    plan.setStatus("IN_PROGRESS");
                    plan.setUpdatedAt(LocalDateTime.now());
                    deliveryPlanRepository.save(plan);
                }
            }

            return String.format("Successfully sent schedule to %d drivers", sentCount);

        } catch (Exception e) {
            log.error("Error sending schedule", e);
            throw new RuntimeException("Failed to send schedule: " + e.getMessage());
        }
    }

    public DeliveryPlanResponse getDeliveryPlan(String id) {
        DeliveryPlan plan = deliveryPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery plan not found"));
        return buildDeliveryPlanResponse(plan);
    }

    public List<DeliveryPlanResponse> getDeliveryPlansByDate(LocalDate date) {
        List<DeliveryPlan> plans = deliveryPlanRepository.findByDeliveryDateOrderByCreatedAtDesc(date);
        return plans.stream().map(this::buildDeliveryPlanResponse).collect(Collectors.toList());
    }

    // Helper methods
    private VrpSolveRequest convertToVrpRequest(CreateDeliveryPlanRequest request) {
        VrpSolveRequest vrpRequest = new VrpSolveRequest();

        // Convert customers
        List<VrpSolveRequest.CustomerData> customers = request.getCustomers().stream()
                .map(c -> new VrpSolveRequest.CustomerData(c.getId(), c.getLat(), c.getLon(),
                        c.getWeight(), c.getTimeWindowStart(), c.getTimeWindowEnd(), c.getServiceTime()))
                .collect(Collectors.toList());

        vrpRequest.setCustomers(customers);
        vrpRequest.setVehicle(new VrpSolveRequest.VehicleConfig(request.getVehicle().getCapacity(), request.getVehicle().getCount()));
        vrpRequest.setDepot(new VrpSolveRequest.DepotData(request.getDepot().getLat(), request.getDepot().getLon()));

        return vrpRequest;
    }

    private List<VrpSolveResponse.RouteData> parseSolutionRoutes(String solutionData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(solutionData,
                    mapper.getTypeFactory().constructCollectionType(List.class, VrpSolveResponse.RouteData.class));
        } catch (Exception e) {
            log.warn("Failed to parse solution data", e);
            return new ArrayList<>();
        }
    }

    private String convertToJsonArray(List<Integer> sequence) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(sequence);
        } catch (Exception e) {
            return "[]";
        }
    }

    private Integer calculateDuration(VrpSolveResponse.RouteData route) {
        // Simple estimation: distance * 2 minutes per km
        return (int) Math.ceil(route.getRouteDistance() * 2);
    }

    private DeliveryPlanResponse buildDeliveryPlanResponse(DeliveryPlan plan) {
        DeliveryPlanResponse response = new DeliveryPlanResponse();
        response.setId(plan.getId());
        response.setPlanName(plan.getPlanName());
        response.setDeliveryDate(plan.getDeliveryDate());
        response.setCoordinatorId(plan.getCoordinatorId());
        response.setStatus(plan.getStatus());
        response.setTotalCustomers(plan.getTotalCustomers());
        response.setNotes(plan.getNotes());
        response.setCreatedAt(plan.getCreatedAt());

        // TODO: Load VRP solution info and assignments
        // This would require additional queries to get full data

        return response;
    }
}
