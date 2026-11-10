package com.adricarrera.orderservice.order.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Enumerated(EnumType.STRING)
  private OrderStatus status;
  @CreationTimestamp
  private LocalDateTime createdAt;
  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderLine> orderLines = new ArrayList<>();

  public void addLine(OrderLine line){
    orderLines.add(line);
    line.setOrder(this);
  }

  public boolean confirm(){
    if(status != OrderStatus.PENDING){
      return false;
    }
    status = OrderStatus.CONFIRMED;
    return true;
  }

  public  boolean reject(){
    if(status != OrderStatus.PENDING){
      return false;
    }
    status = OrderStatus.REJECTED;
    return true;
  }

}
