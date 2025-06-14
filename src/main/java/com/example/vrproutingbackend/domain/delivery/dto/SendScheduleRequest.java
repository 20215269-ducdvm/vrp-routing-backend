package com.example.vrproutingbackend.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendScheduleRequest {
    private String deliveryPlanId;
    private List<Integer> vehicleIndexes; // Chọn những route nào để gửi
}
