package com.adricarrera.inventoryservice.stock.application;

import com.adricarrera.inventoryservice.stock.domain.Product;
import com.adricarrera.inventoryservice.stock.infrastructure.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StockService {

  private final ProductRepository productRepository;

  @Transactional
  public boolean reserve(List<ReservationLine> lines) {

    //1. Check if all products have enough stock
    for (ReservationLine line : lines) {
      Product product = productRepository.findById(line.productId()).orElse(null);
      if (product == null || product.getAvailableQuantity() < line.quantity()) {
        return false;
      }
    }

    //2. Decrease the stock for each product
    for (ReservationLine line : lines) {
      Product product = productRepository.findById(line.productId()).orElseThrow();
      product.decrease(line.quantity());
    }
    return true;
  }
}
