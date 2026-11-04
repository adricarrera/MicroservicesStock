package com.adricarrera.inventoryservice.stock.events;

import com.adricarrera.inventoryservice.stock.application.ReservationLine;
import com.adricarrera.inventoryservice.stock.application.StockService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class OrderCreatedListener {

  private final StockService stockService;

  @KafkaListener(topics = "orders.created")
  public void onOrderCreated(final OrderCreated event) {
    log.info("Received OrderCreated event: {}", event);

    //Convert event lines to reservation lines
    List<ReservationLine> reservationLines = event.lines()
        .stream()
        .map(line -> new ReservationLine(
            line.productId(),
            line.quantity()
        ))
        .toList();

    //Reserve stock
    this.stockService.reserve(reservationLines, event.orderId());
  }
}