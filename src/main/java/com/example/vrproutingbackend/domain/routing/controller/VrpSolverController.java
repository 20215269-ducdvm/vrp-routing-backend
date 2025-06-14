package com.example.vrproutingbackend.domain.routing.controller;

import com.example.vrproutingbackend.domain.routing.dto.VrpSolveRequest;
import com.example.vrproutingbackend.domain.routing.dto.VrpSolveResponse;
import com.example.vrproutingbackend.domain.routing.entity.VrpProblem;
import com.example.vrproutingbackend.domain.routing.repository.VrpProblemRepository;
import com.example.vrproutingbackend.domain.routing.service.VrpSolverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vrp")
@Slf4j
public class VrpSolverController {

    @Autowired
    private VrpSolverService vrpSolverService;

    @PostMapping("/solve")
    public ResponseEntity<VrpSolveResponse> solveProblem(@RequestBody VrpSolveRequest request) {
        log.info("Received VRP solve request with {} customers", request.getCustomers().size());

        // Basic validation
        if (request.getCustomers() == null || request.getCustomers().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new VrpSolveResponse(null, "ERROR", "No customers provided", null)
            );
        }

        if (request.getVehicle() == null) {
            return ResponseEntity.badRequest().body(
                    new VrpSolveResponse(null, "ERROR", "Vehicle configuration missing", null)
            );
        }

        if (request.getDepot() == null) {
            return ResponseEntity.badRequest().body(
                    new VrpSolveResponse(null, "ERROR", "Depot information missing", null)
            );
        }

        VrpSolveResponse response = vrpSolverService.solveProblem(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Autowired
    private VrpProblemRepository vrpProblemRepository;

    @GetMapping("/problems/{id}")
    public ResponseEntity<VrpProblem> getProblem(@PathVariable String id) {
        Optional<VrpProblem> problem = vrpProblemRepository.findById(id);
        return problem.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/problems")
    public ResponseEntity<List<VrpProblem>> getAllProblems() {
        List<VrpProblem> problems = vrpProblemRepository.findAll();
        return ResponseEntity.ok(problems);
    }
}