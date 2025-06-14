package com.example.vrproutingbackend.domain.delivery.repository;

import com.example.vrproutingbackend.domain.delivery.entity.DeliveryPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryPlanRepository extends JpaRepository<DeliveryPlan, String> {
    List<DeliveryPlan> findByDeliveryDateAndCoordinatorIdOrderByCreatedAtDesc(LocalDate deliveryDate, String coordinatorId);
    List<DeliveryPlan> findByDeliveryDateOrderByCreatedAtDesc(LocalDate deliveryDate);
    List<DeliveryPlan> findByStatus(String status);
}
