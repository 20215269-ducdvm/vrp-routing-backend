package com.example.vrproutingbackend.domain.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VrpSolveResponse {
    private String problemType; // "CVRP" or "VRPTW"
    private String status;      // "SUCCESS" or "ERROR"
    private String message;
    private SolutionData solution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolutionData {
        private double totalDistance;
        private double totalCost;
        private int vehiclesUsed;
        private List<RouteData> routes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteData {
        private int vehicleId;
        private List<Integer> customerSequence;
        private double routeDistance;
        private List<Integer> arrivalTimes;
    }
}
