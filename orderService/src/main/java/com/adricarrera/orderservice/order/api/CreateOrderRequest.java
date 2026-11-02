package com.adricarrera.orderservice.order.api;


import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    List<OrderLineRequest> lines
) {

  public record OrderLineRequest(
      UUID productId,
      Integer quantity
  ) {}
}
