package com.example.vrproutingbackend.domain.routing.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vrp_customer")
@Data
public class VrpCustomer {
    @Id
    private String id;

    @Column(name = "vrp_problem_id")
    private String vrpProblemId;

    @Column(name = "customer_index")
    private Integer customerIndex;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "demand")
    private Double demand;

    @Column(name = "service_time")
    private Integer serviceTime;

    @Column(name = "time_window_start")
    private Integer timeWindowStart;

    @Column(name = "time_window_end")
    private Integer timeWindowEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vrp_problem_id", insertable = false, updatable = false)
    private VrpProblem vrpProblem;
}
