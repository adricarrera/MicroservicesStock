package com.adricarrera.inventoryservice.stock.events;

import java.util.UUID;

public record StockRejected(
    int version,
    UUID orderId,
    String reason
) {
}
