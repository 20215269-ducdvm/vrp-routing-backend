package com.example.vrproutingbackend.domain.routing.service;

import com.example.vrproutingbackend.domain.routing.dto.VrpSolveRequest;
import com.example.vrproutingbackend.domain.routing.dto.VrpSolveResponse;
import com.example.vrproutingbackend.domain.routing.entity.RouteSolution;
import com.example.vrproutingbackend.domain.routing.entity.VrpCustomer;
import com.example.vrproutingbackend.domain.routing.entity.VrpProblem;
import com.example.vrproutingbackend.domain.routing.repository.RouteSolutionRepository;
import com.example.vrproutingbackend.domain.routing.repository.VrpCustomerRepository;
import com.example.vrproutingbackend.domain.routing.repository.VrpProblemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VrpSolverService {

    @Autowired
    private VrpProblemRepository vrpProblemRepository;

    @Autowired
    private VrpCustomerRepository vrpCustomerRepository;

    @Autowired
    private RouteSolutionRepository routeSolutionRepository;


    public VrpSolveResponse solveProblem(VrpSolveRequest request) {
        try {
            String problemType = detectProblemType(request);

            VrpProblem problem = createVrpProblem(request, problemType);
            String format;
            if ("CVRP".equals(problemType)) {
                format = convertToVRPLIBFormat(request);
                log.info("Converted VRP request to VRPLIB format:\n");
            } else {
                format = convertToSolomonFormat(request);
                log.info("Converting VRP request to Solomon format");
            }

            problem.setStatus("CREATED");
            vrpProblemRepository.save(problem);
            saveCustomers(problem.getId(), request.getCustomers());

            VrpSolveResponse.SolutionData solution = callPythonSolver(format, request.getAdditionalArgs());

            saveSolution(problem.getId(), solution);
            problem.setStatus("SOLVED");
            vrpProblemRepository.save(problem);

            return new VrpSolveResponse(problemType, "SUCCESS", "Problem solved successfully", solution);

        } catch (Exception e) {
            log.error("Error solving VRP problem", e);
            return new VrpSolveResponse(null, "ERROR", e.getMessage(), null);
        }
    }

    private String detectProblemType(VrpSolveRequest request) {
        boolean hasTimeWindow = request.getCustomers().stream()
                .anyMatch(c -> c.getTimeWindowStart() != null && c.getTimeWindowEnd() != null);
        return hasTimeWindow ? "VRPTW" : "CVRP";
    }

    private VrpProblem createVrpProblem(VrpSolveRequest request, String problemType) {
        VrpProblem problem = new VrpProblem();
        problem.setId(UUID.randomUUID().toString());
        problem.setProblemType(problemType);
        problem.setCustomerCount(request.getCustomers().size());
        problem.setVehicleCapacity(request.getVehicle().getCapacity());
        problem.setVehicleCount(request.getVehicle().getCount());
        problem.setDepotLat(request.getDepot().getLat());
        problem.setDepotLon(request.getDepot().getLon());
        problem.setCreatedAt(LocalDateTime.now());
        return problem;
    }

    private void saveCustomers(String problemId, List<VrpSolveRequest.CustomerData> customers) {
        for (int i = 0; i < customers.size(); i++) {
            VrpSolveRequest.CustomerData customerData = customers.get(i);
            VrpCustomer customer = new VrpCustomer();
            customer.setId(UUID.randomUUID().toString());
            customer.setVrpProblemId(problemId);
            customer.setCustomerIndex(i + 1);
            customer.setLat(customerData.getLat());
            customer.setLon(customerData.getLon());
            customer.setDemand(customerData.getWeight());
            customer.setServiceTime(customerData.getServiceTime() != null ? customerData.getServiceTime() : 0);
            customer.setTimeWindowStart(customerData.getTimeWindowStart());
            customer.setTimeWindowEnd(customerData.getTimeWindowEnd());
            vrpCustomerRepository.save(customer);
        }
    }
    // TODO: integrate with actual algorithms
    private void saveSolution(String problemId, VrpSolveResponse.SolutionData solution) {
        RouteSolution routeSolution = new RouteSolution();
        routeSolution.setId(UUID.randomUUID().toString());
        routeSolution.setVrpProblemId(problemId);
        routeSolution.setAlgorithmName("mock_algorithm");
        routeSolution.setTotalCost(solution.getTotalCost());
        routeSolution.setTotalDistance(solution.getTotalDistance());
        routeSolution.setVehiclesUsed(solution.getVehiclesUsed());
        routeSolution.setExecutionTimeSeconds(1.5);
        routeSolution.setStatus("COMPLETED");
        routeSolution.setSolvedAt(LocalDateTime.now());

        // Convert routes to JSON string
        try {
            ObjectMapper mapper = new ObjectMapper();
            routeSolution.setSolutionData(mapper.writeValueAsString(solution.getRoutes()));
        } catch (Exception e) {
            log.warn("Failed to serialize solution data", e);
        }

        routeSolutionRepository.save(routeSolution);
    }

    private String convertToSolomonFormat(VrpSolveRequest request) {
        StringBuilder solomon = new StringBuilder();

        solomon.append("VEHICLE\n");
        solomon.append("NUMBER     CAPACITY\n");
        solomon.append(String.format("%8d %12.0f\n", request.getVehicle().getCount(), request.getVehicle().getCapacity()));
        solomon.append("\n");

        solomon.append("CUSTOMER\n");

        solomon.append("CUST NO.  XCOORD.   YCOORD.    DEMAND   READY TIME  DUE DATE   SERVICE TIME\n");
        solomon.append(String.format("%8d %10.2f %10.2f %10.0f %10d %10d %10d\n",
                0, request.getDepot().getLat(), request.getDepot().getLon(), 0.0, 0, 1440, 0));

        for (VrpSolveRequest.CustomerData customer : request.getCustomers()) {
            solomon.append(String.format("%8d %10.2f %10.2f %10.2f %10d %10d %10d\n",
                    customer.getId(), customer.getLat(), customer.getLon(), customer.getWeight(),
                    customer.getTimeWindowStart() != null ? customer.getTimeWindowStart() : 0,
                    customer.getTimeWindowEnd() != null ? customer.getTimeWindowEnd() : 1440,
                    customer.getServiceTime() != null ? customer.getServiceTime() : 0));
        }

        return solomon.toString();
    }

    private String convertToVRPLIBFormat(VrpSolveRequest request) {
        StringBuilder vrplib = new StringBuilder();

        // Header section
        vrplib.append("NAME : problem_").append(UUID.randomUUID().toString().substring(0, 8)).append(".vrp\n");
        vrplib.append("COMMENT : Auto-generated VRP problem\n");
        vrplib.append("TYPE : ").append("CVRP").append("\n");
        vrplib.append("DIMENSION : ").append(request.getCustomers().size() + 1).append("\n");
        vrplib.append("EDGE_WEIGHT_TYPE : EUC_2D\n");
        vrplib.append("CAPACITY : ").append((int)request.getVehicle().getCapacity()).append("\n");

        // Node coordinates section
        vrplib.append("NODE_COORD_SECTION\n");
        // Depot is node 1
        vrplib.append("1 ").append(request.getDepot().getLat()).append(" ").append(request.getDepot().getLon()).append("\n");

        // Customers start from node 2
        int nodeId = 2;
        for (VrpSolveRequest.CustomerData customer : request.getCustomers()) {
            vrplib.append(nodeId++).append(" ")
                    .append(customer.getLat()).append(" ")
                    .append(customer.getLon()).append("\n");
        }

        // Demand section
        vrplib.append("DEMAND_SECTION\n");
        // Depot has demand 0
        vrplib.append("1 0\n");

        // Customer demands
        nodeId = 2;
        for (VrpSolveRequest.CustomerData customer : request.getCustomers()) {
            vrplib.append(nodeId++).append(" ").append((int)customer.getWeight()).append("\n");
        }

        // Depot section
        vrplib.append("DEPOT_SECTION\n");
        vrplib.append("1\n");
        vrplib.append("-1\n");

        vrplib.append("EOF\n");

        return vrplib.toString();
    }

    private VrpSolveResponse.SolutionData callPythonSolver(String format, String additionalArgs) {
        try {
            Path projectRoot = Path.of("").toAbsolutePath();
            Path algorithmDir = projectRoot.resolve("hybrid-hs-vrp/algorithm");

            Path generatedDirPath = algorithmDir.resolve("instances/generated");
            Files.createDirectories(generatedDirPath);

            String filename = "problem_" + UUID.randomUUID().toString().substring(0, 8) + ".vrp";
            Path instanceFile = generatedDirPath.resolve(filename);
            Files.writeString(instanceFile, format);

            String relativeInstancePath = "instances/generated/" + filename;

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    "main.py",
                    "--instance=" + relativeInstancePath
            );

            if (additionalArgs != null && !additionalArgs.trim().isEmpty()) {
                // Handle arguments in the format --key=value
                String[] arguments = Arrays.stream(additionalArgs.trim().split("(?=--)"))
                        .filter(arg -> !arg.isEmpty())
                        .map(String::trim)
                        .toArray(String[]::new);

                for (String arg : arguments) {
                    processBuilder.command().add(arg);
                }
            }

            processBuilder.directory(algorithmDir.toFile());

            processBuilder.redirectErrorStream(true);

            log.info("Instance file created at: {}", instanceFile);
            log.info("Executing Python solver: {}", processBuilder.command());

            Process process = processBuilder.start();

            // Read the combined output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script exited with code " + exitCode + ": " + output);
            }

            log.info("Python solver output: {}", output);

            // Parse the output to get the solution
            return parseSolverOutput(output.toString());

        } catch (Exception e) {
            log.error("Error calling Python solver", e);
            throw new RuntimeException("Failed to execute Python solver", e);
        }
    }

    private VrpSolveResponse.SolutionData parseSolverOutput(String output) {
        try {
            List<VrpSolveResponse.RouteData> routes = new ArrayList<>();
            double totalCost = 0;
            int vehiclesUsed = 0;

            // Current route being processed
            Integer routeNumber = null;
            List<Integer> routeNodes = null;
            List<Integer> arrivalTimes = null;
            double routeDistance = 0;

            // Split the output by lines
            String[] lines = output.split("\n");

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Route #")) {
                    // Complete previous route if exists
                    if (routeNumber != null && routeNodes != null) {
                        routes.add(new VrpSolveResponse.RouteData(routeNumber, routeNodes, routeDistance, arrivalTimes));
                    }

                    vehiclesUsed++;

                    // Extract route number and nodes in the route
                    routeNumber = Integer.parseInt(line.substring(7, line.indexOf(":")));

                    // The example "Route #1: 1 4" means the route has nodes 1 and 4
                    String nodesStr = line.substring(line.indexOf(":") + 1).trim();
                    routeNodes = Arrays.stream(nodesStr.split("\\s+"))
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    // Reset other values
                    arrivalTimes = null;
                    routeDistance = 0;

                } else if (line.startsWith("Arrival times:")) {
                    // Parse arrival times list
                    String timesString = line.substring(line.indexOf("[") + 1, line.lastIndexOf("]"));
                    String[] timesArray = timesString.split(",\\s*");
                    arrivalTimes = Arrays.stream(timesArray)
                            .map(s -> (int)Math.round(Double.parseDouble(s)))
                            .collect(Collectors.toList());

                } else if (line.startsWith("Route distance:")) {
                    // Extract route distance
                    routeDistance = Double.parseDouble(line.substring(line.indexOf(":") + 1).trim());

                } else if (line.startsWith("Cost:")) {
                    totalCost = Double.parseDouble(line.substring(5).trim());

                    // Add the last route if we've reached the end
                    if (routeNumber != null && routeNodes != null) {
                        routes.add(new VrpSolveResponse.RouteData(routeNumber, routeNodes, routeDistance, arrivalTimes));
                        routeNumber = null;
                        routeNodes = null;
                    }
                }
            }

            // If there's still a route being processed (no Cost line at the end)
            if (routeNumber != null && routeNodes != null) {
                routes.add(new VrpSolveResponse.RouteData(routeNumber, routeNodes, routeDistance, arrivalTimes));
            }

            return new VrpSolveResponse.SolutionData(totalCost, totalCost, vehiclesUsed, routes);
        } catch (Exception e) {
            log.error("Failed to parse solver output", e);
            throw new RuntimeException("Failed to parse solver output: " + e.getMessage(), e);
        }
    }
}
