package com.example.vrproutingbackend.domain.routing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_solution")
@Data
public class RouteSolution {
    @Id
    private String id;

    @Column(name = "vrp_problem_id")
    private String vrpProblemId;

    @Column(name = "algorithm_name")
    private String algorithmName;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "vehicles_used")
    private Integer vehiclesUsed;

    @Column(name = "execution_time_seconds")
    private Double executionTimeSeconds;

    @Column(name = "solution_data", columnDefinition = "TEXT")
    private String solutionData; // JSON string

    @Column(name = "status")
    private String status; // "PENDING", "COMPLETED", "FAILED"

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vrp_problem_id", insertable = false, updatable = false)
    @JsonIgnore
    private VrpProblem vrpProblem;
}
