package com.example.vrproutingbackend.domain.delivery.entity;

import com.example.vrproutingbackend.domain.routing.entity.VrpProblem;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "delivery_plan")
@Data
public class DeliveryPlan {
    @Id
    private String id;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "coordinator_id")
    private String coordinatorId;

    @Column(name = "status")
    private String status; // DRAFT, OPTIMIZED, ASSIGNED, IN_PROGRESS, COMPLETED

    @Column(name = "vrp_problem_id")
    private String vrpProblemId;

    @Column(name = "total_customers")
    private Integer totalCustomers;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vrp_problem_id", insertable = false, updatable = false)
    private VrpProblem vrpProblem;

    @OneToMany(mappedBy = "deliveryPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryAssignment> assignments;
}
