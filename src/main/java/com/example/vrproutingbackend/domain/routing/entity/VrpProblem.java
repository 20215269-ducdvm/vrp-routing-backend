package com.example.vrproutingbackend.domain.routing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vrp_problem")
@Data
public class VrpProblem {
    @Id
    private String id;

    @Column(name = "problem_type")
    private String problemType; // "CVRP" or "VRPTW"

    @Column(name = "customer_count")
    private Integer customerCount;

    @Column(name = "vehicle_capacity")
    private Double vehicleCapacity;

    @Column(name = "vehicle_count")
    private Integer vehicleCount;

    @Column(name = "depot_lat")
    private Double depotLat;

    @Column(name = "depot_lon")
    private Double depotLon;

    @Column(name = "status")
    private String status; // "CREATED", "SOLVING", "SOLVED", "FAILED"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "vrpProblem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RouteSolution> solutions;
}
