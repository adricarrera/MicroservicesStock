package com.adricarrera.inventoryservice.stock.domain;

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
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String name;
  private int availableQuantity;


  public void decrease(int quantity){
    if(quantity > availableQuantity){
      throw new IllegalStateException("Not enough stock available for product " + name);
    }
    availableQuantity -= quantity;
  }

}