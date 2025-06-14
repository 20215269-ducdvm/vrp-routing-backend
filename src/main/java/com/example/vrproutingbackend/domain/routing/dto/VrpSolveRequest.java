package com.example.vrproutingbackend.domain.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VrpSolveRequest {
    private List<CustomerData> customers;
    private VehicleConfig vehicle;
    private DepotData depot;
    private String additionalArgs; // JSON string for additional parameters

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerData {
        private int id;
        private double lat;
        private double lon;
        private double weight;
        private Integer timeWindowStart; // minutes from depot opening, nullable
        private Integer timeWindowEnd;   // minutes from depot opening, nullable
        private Integer serviceTime;     // minutes, nullable
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleConfig {
        private double capacity;
        private int count; // max number of vehicles
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepotData {
        private double lat;
        private double lon;
    }
}
