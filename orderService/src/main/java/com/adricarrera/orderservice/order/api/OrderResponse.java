package com.adricarrera.orderservice.order.api;

import com.adricarrera.orderservice.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
    UUID id,
    OrderStatus status,
    LocalDateTime createdAt,
    List<OrderLineResponse> lines
) {
  public record OrderLineResponse(
      UUID productId,
      Integer quantity
  ) {}
}
