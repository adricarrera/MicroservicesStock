package com.adricarrera.inventoryservice.stock.events;

import java.util.List;
import java.util.UUID;

public record OrderCreated(int version, UUID orderId, List<Line> lines) {
  public record Line(UUID productId, int quantity) {}
}
