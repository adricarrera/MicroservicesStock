package com.adricarrera.notificationservice.notification.events;

import java.util.UUID;

public record StockReserved (
    int version,
    UUID orderId
){
}
