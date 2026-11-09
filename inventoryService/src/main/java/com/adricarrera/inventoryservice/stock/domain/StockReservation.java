package com.adricarrera.inventoryservice.stock.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StockReservation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;
  UUID orderId;
  @Enumerated(EnumType.STRING)
  ReservationStatus status;
  @CreationTimestamp
  LocalDateTime createdAt;
}
