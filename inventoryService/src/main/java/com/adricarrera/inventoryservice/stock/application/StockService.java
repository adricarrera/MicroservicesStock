package com.adricarrera.inventoryservice.stock.application;

import com.adricarrera.inventoryservice.stock.domain.Product;
import com.adricarrera.inventoryservice.stock.events.StockEventPublisher;
import com.adricarrera.inventoryservice.stock.events.StockRejected;
import com.adricarrera.inventoryservice.stock.events.StockReserved;
import com.adricarrera.inventoryservice.stock.infrastructure.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class StockService {

  private final ProductRepository productRepository;
  private final StockEventPublisher stockEventPublisher;

  @Transactional
  public void reserve(final List<ReservationLine> lines, final UUID orderId) {
    //1. Check if all products have enough stock
    final boolean hasInsufficientStock = lines.stream()
        .anyMatch(line -> {
          Product product = this.productRepository.findById(line.productId()).orElse(null);
          return product == null || product.getAvailableQuantity() < line.quantity();
        });

    if (hasInsufficientStock) {
      log.warn("Stock reservation failed for order {}", orderId);
      this.stockEventPublisher.publishStockRejected(new StockRejected(1, orderId, "Insufficient stock"));

    } else {

      //2. Decrease the stock for each product
      lines.forEach(
          line -> {
            Product product = this.productRepository.findById(line.productId()).orElseThrow();
            product.decrease(line.quantity());
          });
      log.info("Stock reserved for order {}", orderId);
      this.stockEventPublisher.publishStockReserved(new StockReserved(1, orderId));
    }
  }
}
