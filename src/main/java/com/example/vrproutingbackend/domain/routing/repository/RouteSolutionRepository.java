package com.example.vrproutingbackend.domain.routing.repository;

import com.example.vrproutingbackend.domain.routing.entity.RouteSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteSolutionRepository extends JpaRepository<RouteSolution, String> {
    List<RouteSolution> findByVrpProblemIdOrderBySolvedAtDesc(String vrpProblemId);
    Optional<RouteSolution> findByVrpProblemIdAndStatus(String vrpProblemId, String status);
}