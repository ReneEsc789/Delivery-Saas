package com.deliverysaas.orders;
import java.util.*;import org.springframework.http.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;import com.deliverysaas.shared.security.AuthPrincipal;import jakarta.validation.Valid;
@RestController @RequestMapping("/api/v1/orders") public class OrderController{
 private final OrderService service;public OrderController(OrderService s){service=s;}
 @PostMapping public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateOrderRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p,r));}
 @GetMapping public List<OrderResponse> all(@AuthenticationPrincipal AuthPrincipal p){return service.all(p);}@GetMapping("/{id}") public OrderResponse one(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){return service.one(p,id);}
 @PutMapping("/{id}") public OrderResponse update(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody UpdateOrderRequest r){return service.update(p,id,r);}
 @PostMapping("/{id}/confirm") public OrderResponse confirm(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){return service.confirm(p,id);}
 @PostMapping("/{id}/status") public OrderResponse status(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody OrderStatusRequest r){return service.changeStatus(p,id,r);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){service.cancel(p,id);return ResponseEntity.noContent().build();}
}
