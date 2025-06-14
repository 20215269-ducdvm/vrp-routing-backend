package com.example.vrproutingbackend.domain.delivery.entity;

import com.example.vrproutingbackend.domain.routing.entity.RouteSolution;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_assignment")
@Data
public class DeliveryAssignment {
    @Id
    private String id;

    @Column(name = "delivery_plan_id")
    private String deliveryPlanId;

    @Column(name = "route_solution_id")
    private String routeSolutionId;

    @Column(name = "vehicle_index")
    private Integer vehicleIndex;

    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "customer_sequence", columnDefinition = "TEXT")
    private String customerSequence; // JSON: [0,1,2,0]

    @Column(name = "estimated_distance")
    private Double estimatedDistance;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "status")
    private String status; // ASSIGNED, SENT, ACCEPTED, IN_PROGRESS, COMPLETED

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_plan_id", insertable = false, updatable = false)
    private DeliveryPlan deliveryPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_solution_id", insertable = false, updatable = false)
    private RouteSolution routeSolution;
}

