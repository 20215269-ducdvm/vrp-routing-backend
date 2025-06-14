package com.example.vrproutingbackend.domain.delivery.controller;

import com.example.vrproutingbackend.domain.delivery.dto.AssignDriverRequest;
import com.example.vrproutingbackend.domain.delivery.dto.CreateDeliveryPlanRequest;
import com.example.vrproutingbackend.domain.delivery.dto.DeliveryPlanResponse;
import com.example.vrproutingbackend.domain.delivery.dto.SendScheduleRequest;
import com.example.vrproutingbackend.domain.delivery.service.DeliveryPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@Slf4j
public class DeliveryPlanController {

    @Autowired
    private DeliveryPlanService deliveryPlanService;

    // Tạo kế hoạch giao hàng mới
    @PostMapping("/plans")
    public ResponseEntity<DeliveryPlanResponse> createDeliveryPlan(@RequestBody CreateDeliveryPlanRequest request) {
        log.info("Creating delivery plan: {} for date: {}", request.getPlanName(), request.getDeliveryDate());

        // Basic validation
        if (request.getPlanName() == null || request.getPlanName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getDeliveryDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getCustomers() == null || request.getCustomers().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            DeliveryPlanResponse response = deliveryPlanService.createDeliveryPlan(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create delivery plan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Phân công tài xế cho các tuyến
    @PostMapping("/plans/{id}/assign-drivers")
    public ResponseEntity<DeliveryPlanResponse> assignDrivers(
            @PathVariable String id,
            @RequestBody AssignDriverRequest request) {

        log.info("Assigning drivers to delivery plan: {}", id);

        if (!id.equals(request.getDeliveryPlanId())) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            DeliveryPlanResponse response = deliveryPlanService.assignDrivers(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to assign drivers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Gửi lịch cho tài xế
    @PostMapping("/plans/{id}/send-schedule")
    public ResponseEntity<Map<String, String>> sendSchedule(
            @PathVariable String id,
            @RequestBody SendScheduleRequest request) {

        log.info("Sending schedule for delivery plan: {}", id);

        if (!id.equals(request.getDeliveryPlanId())) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getVehicleIndexes() == null || request.getVehicleIndexes().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String result = deliveryPlanService.sendScheduleToDrivers(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send schedule", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy thông tin kế hoạch giao hàng
    @GetMapping("/plans/{id}")
    public ResponseEntity<DeliveryPlanResponse> getDeliveryPlan(@PathVariable String id) {
        try {
            DeliveryPlanResponse response = deliveryPlanService.getDeliveryPlan(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get delivery plan: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy danh sách kế hoạch theo ngày
    @GetMapping("/plans")
    public ResponseEntity<List<DeliveryPlanResponse>> getDeliveryPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDate searchDate = date != null ? date : LocalDate.now();
            List<DeliveryPlanResponse> plans = deliveryPlanService.getDeliveryPlansByDate(searchDate);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            log.error("Failed to get delivery plans for date: {}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy assignments của một plan (để xem ai được phân công)
    @GetMapping("/plans/{id}/assignments")
    public ResponseEntity<List<DeliveryPlanResponse.AssignmentInfo>> getAssignments(@PathVariable String id) {
        try {
            DeliveryPlanResponse plan = deliveryPlanService.getDeliveryPlan(id);
            List<DeliveryPlanResponse.AssignmentInfo> assignments = plan.getAssignments();
            return ResponseEntity.ok(assignments != null ? assignments : new ArrayList<>());
        } catch (Exception e) {
            log.error("Failed to get assignments for plan: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
