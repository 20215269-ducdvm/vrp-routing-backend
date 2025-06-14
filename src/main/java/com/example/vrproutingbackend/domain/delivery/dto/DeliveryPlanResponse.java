package com.example.vrproutingbackend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPlanResponse {
    private String id;
    private String planName;
    private LocalDate deliveryDate;
    private String coordinatorId;
    private String status;
    private Integer totalCustomers;
    private String notes;
    private LocalDateTime createdAt;

    // VRP Solution info
    private VrpSolutionInfo vrpSolution;

    // Driver assignments
    private List<AssignmentInfo> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VrpSolutionInfo {
        private String problemId;
        private String problemType;
        private String problemStatus;
        private Double totalDistance;
        private Double totalCost;
        private Integer vehiclesUsed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentInfo {
        private String id;
        private Integer vehicleIndex;
        private String driverId;
        private String driverName;
        private String driverPhone;
        private List<Integer> customerSequence;
        private Double estimatedDistance;
        private Integer estimatedDuration;
        private String status;
        private LocalDateTime assignedAt;
        private LocalDateTime sentAt;
    }
}

