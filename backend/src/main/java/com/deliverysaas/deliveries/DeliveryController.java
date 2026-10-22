package com.deliverysaas.deliveries;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.deliveries.assignment.AssignDeliveryService;
import com.deliverysaas.deliveries.prioritization.DeliveryPriorityService;
import com.deliverysaas.deliveries.prioritization.DispatchResponse;
import com.deliverysaas.deliveries.prioritization.PendingDispatchService;
import com.deliverysaas.deliveries.prioritization.PrioritizedDeliveryResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController{
    private final DeliveryService service;
    private final AssignDeliveryService autoAssign;
    private final DeliveryPriorityService priority;
    private final PendingDispatchService dispatch;
    public DeliveryController(DeliveryService s, AssignDeliveryService a, DeliveryPriorityService pr, PendingDispatchService d){
        service=s;
        autoAssign = a;
        priority = pr;
        dispatch = d;
    }
    @PostMapping 
    public ResponseEntity<DeliveryResponse> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateDeliveryRequest r){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p,r));
    }
    @GetMapping 
    public List<DeliveryResponse> all(@AuthenticationPrincipal AuthPrincipal p){
        return service.all(p);
    }
    @GetMapping("/queue")
    public List<PrioritizedDeliveryResponse> queue(@AuthenticationPrincipal AuthPrincipal p){
        return priority.queue(p);
    }
    @PostMapping("/auto-assign-pending")
    public DispatchResponse autoAssignPending(@AuthenticationPrincipal AuthPrincipal p){
        return dispatch.autoAssignPending(p);
    }
    @GetMapping("/{id}")
    public DeliveryResponse one(@AuthenticationPrincipal AuthPrincipal p, @PathVariable UUID id){
        return service.one(p,id);
    }
    @PutMapping("/{id}")
    public DeliveryResponse update(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody UpdateDeliveryRequest r){
        return service.update(p,id,r);
    }
    @PutMapping("/{id}/assignment")
    public DeliveryResponse assign(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody AssignDeliveryRequest r){
        return service.assign(p,id,r);
    }

    @PostMapping("/{id}/auto-assignment")
    public DeliveryResponse autoAssign(@AuthenticationPrincipal AuthPrincipal p, @PathVariable UUID id) {
        return autoAssign.autoAssign(p, id);
    }
    
    @PostMapping("/{id}/status")
    public DeliveryResponse status(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody DeliveryStatusRequest r){
        return service.status(p,id,r);
    }
    @GetMapping("/{id}/history")
    public List<DeliveryHistoryResponse> history(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        return service.history(p,id);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        service.cancel(p,id);
        return ResponseEntity.noContent().build();
    }
}
