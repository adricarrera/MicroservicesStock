package com.adricarrera.orderservice.order.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderLine {
  @Id
  @GeneratedValue (strategy = GenerationType.UUID)
  private UUID id;
  private UUID productId;
  private Integer quantity;
  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;
}
