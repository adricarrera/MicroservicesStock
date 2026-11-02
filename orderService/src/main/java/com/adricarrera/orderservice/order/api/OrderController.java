package com.adricarrera.orderservice.order.api;

import com.adricarrera.orderservice.order.application.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable final UUID id) {
    return ResponseEntity.ok(orderService.getOrder(id));
  }

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@RequestBody final CreateOrderRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(orderService.createOrder(request));
  }

}
