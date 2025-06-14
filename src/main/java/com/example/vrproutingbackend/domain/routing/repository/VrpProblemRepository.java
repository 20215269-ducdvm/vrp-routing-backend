package com.example.vrproutingbackend.domain.routing.repository;

import com.example.vrproutingbackend.domain.routing.entity.VrpProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VrpProblemRepository extends JpaRepository<VrpProblem, String> {
    List<VrpProblem> findByProblemType(String problemType);
    List<VrpProblem> findByStatus(String status);
}