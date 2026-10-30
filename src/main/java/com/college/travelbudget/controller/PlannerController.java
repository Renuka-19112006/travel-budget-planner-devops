package com.college.travelbudget.controller;

import com.college.travelbudget.model.PlanRequest;
import com.college.travelbudget.model.PlanResponse;
import com.college.travelbudget.service.PlannerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlannerController {

    private final PlannerService plannerService;

    public PlannerController(PlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @GetMapping("/destinations")
    public List<String> destinations() {
        return plannerService.getDestinations();
    }

    @GetMapping("/cities")
    public List<String> cities() {
        return plannerService.getCities();
    }

    @PostMapping("/plan")
    public PlanResponse plan(@Valid @RequestBody PlanRequest request) {
        return plannerService.plan(request);
    }
}
