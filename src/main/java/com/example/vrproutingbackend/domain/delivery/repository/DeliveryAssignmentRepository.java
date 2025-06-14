package com.example.vrproutingbackend.domain.delivery.repository;

import com.example.vrproutingbackend.domain.delivery.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, String> {
    List<DeliveryAssignment> findByDeliveryPlanIdOrderByVehicleIndex(String deliveryPlanId);
    List<DeliveryAssignment> findByDriverIdAndStatus(String driverId, String status);
    Optional<DeliveryAssignment> findByDeliveryPlanIdAndVehicleIndex(String deliveryPlanId, Integer vehicleIndex);
}