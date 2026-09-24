package com.deliverysaas.deliveries.scheduling;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverysaas.shared.security.AuthPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deliveries")
public class ScheduleController {

    private final ScheduleService schedules;

    public ScheduleController(ScheduleService schedules) {
        this.schedules = schedules;
    }

    @PostMapping("/schedule-plan")
    public SchedulePlanResponse plan(@AuthenticationPrincipal AuthPrincipal principal,
                                     @Valid @RequestBody SchedulePlanRequest request) {
        return schedules.plan(principal, request);
    }
}
