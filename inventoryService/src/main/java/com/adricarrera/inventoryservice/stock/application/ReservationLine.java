package com.adricarrera.inventoryservice.stock.application;

import java.util.UUID;

public record ReservationLine(UUID productId, int quantity) {
}
