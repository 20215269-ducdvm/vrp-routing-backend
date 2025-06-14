package com.example.vrproutingbackend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryPlanRequest {
    private String planName;
    private LocalDate deliveryDate;
    private String coordinatorId;
    private List<CustomerData> customers;
    private VehicleConfig vehicle;
    private DepotData depot;
    private String notes;

    // Reuse từ VrpSolveRequest
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerData {
        private int id;
        private double lat;
        private double lon;
        private double weight;
        private Integer timeWindowStart;
        private Integer timeWindowEnd;
        private Integer serviceTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleConfig {
        private double capacity;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepotData {
        private double lat;
        private double lon;
    }
}
