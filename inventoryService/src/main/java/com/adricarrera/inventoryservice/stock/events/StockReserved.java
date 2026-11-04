package com.adricarrera.inventoryservice.stock.events;

import java.util.UUID;

public record StockReserved (
    int version,
    UUID orderId
){
}
