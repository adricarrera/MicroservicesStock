package com.adricarrera.orderservice.order.events;

import java.util.UUID;

public record StockReserved(
    int version,
    UUID orderId
) {
}
