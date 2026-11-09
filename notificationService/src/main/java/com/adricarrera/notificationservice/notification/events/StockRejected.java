package com.adricarrera.notificationservice.notification.events;

import java.util.UUID;

public record StockRejected(
    int version,
    UUID orderId,
    String reason
) {
}

