package com.deliverysaas.deliveries.routing;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverysaas.shared.security.AuthPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deliveries")
public class RoutingController {

    private final RouteService routes;
    private final RoutePlanService plans;

    public RoutingController(RouteService routes, RoutePlanService plans) {
        this.routes = routes;
        this.plans = plans;
    }

    @GetMapping("/{id}/route")
    public DeliveryRouteResponse route(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        return routes.route(principal, id);
    }

    @PostMapping("/route-plan")
    public RoutePlanResponse plan(@AuthenticationPrincipal AuthPrincipal principal,
                                  @Valid @RequestBody RoutePlanRequest request) {
        return plans.plan(principal, request);
    }
}
