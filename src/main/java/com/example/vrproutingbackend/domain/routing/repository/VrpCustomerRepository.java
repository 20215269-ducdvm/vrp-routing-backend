package com.example.vrproutingbackend.domain.routing.repository;

import com.example.vrproutingbackend.domain.routing.entity.VrpCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VrpCustomerRepository extends JpaRepository<VrpCustomer, String> {
    List<VrpCustomer> findByVrpProblemIdOrderByCustomerIndex(String vrpProblemId);
}