package com.adricarrera.inventoryservice.stock.infrastructure;

import com.adricarrera.inventoryservice.stock.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

  boolean existsByOrderId(UUID orderId);
}
