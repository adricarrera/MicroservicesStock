package com.adricarrera.inventoryservice.stock.application;

import com.adricarrera.inventoryservice.stock.domain.Product;
import com.adricarrera.inventoryservice.stock.domain.ReservationStatus;
import com.adricarrera.inventoryservice.stock.domain.StockReservation;
import com.adricarrera.inventoryservice.stock.events.StockEventPublisher;
import com.adricarrera.inventoryservice.stock.events.StockRejected;
import com.adricarrera.inventoryservice.stock.events.StockReserved;
import com.adricarrera.inventoryservice.stock.infrastructure.ProductRepository;
import com.adricarrera.inventoryservice.stock.infrastructure.StockReservationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StockService {

  private final ProductRepository productRepository;
  private final StockEventPublisher stockEventPublisher;
  private final StockReservationRepository stockReservationRepository;

  @Transactional
  public void reserve(final List<ReservationLine> lines, final UUID orderId) {
    //Si este pedido ya se procesó, ignorar el mensaje duplicado
    if(this.stockReservationRepository.existsByOrderId(orderId)) {
      log.warn("Order {} already processed, ignoring", orderId);
      return;
    }
    final Map<UUID, Product> products = this.productRepository
        .findAllByIdForUpdate(lines.stream().map(ReservationLine::productId).toList())
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    final boolean hasInsufficientStock = lines.stream()
        .anyMatch(line -> {
          Product product = products.get(line.productId());
          return product == null || product.getAvailableQuantity() < line.quantity();
        });

    if (hasInsufficientStock) {
      saveReservation(orderId, ReservationStatus.REJECTED);
      log.warn("Stock reservation failed for order {}", orderId);
      this.stockEventPublisher.publishStockRejected(new StockRejected(1, orderId, "Insufficient stock"));

    } else {
      //2. Decrease the stock for each product
      lines.forEach(
          line -> {
            Product product = this.productRepository.findById(line.productId()).orElseThrow();
            product.decrease(line.quantity());
          });
      saveReservation(orderId, ReservationStatus.RESERVED);
      log.info("Stock reserved for order {}", orderId);
      this.stockEventPublisher.publishStockReserved(new StockReserved(1, orderId));
    }
  }

  private void saveReservation(final UUID orderId, final ReservationStatus status) {
    this.stockReservationRepository.saveAndFlush(
        StockReservation.builder()
            .orderId(orderId)
            .status(status)
            .build());
  }
}
