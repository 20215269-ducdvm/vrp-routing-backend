package com.example.vrproutingbackend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {
    private String deliveryPlanId;
    private List<DriverAssignment> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverAssignment {
        private int vehicleIndex;
        private String driverId;
        private String driverName;
        private String driverPhone;
    }
}
