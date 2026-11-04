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
  private final StockEventPublisher stockEventPublisher;

  @KafkaListener(topics = "orders.created")
  public void onOrderCreated(OrderCreated event){
    log.info("Received OrderCreated event: {}", event);

    //Convert event lines to reservation lines
    List <ReservationLine> reservationLines = event.lines()
        .stream()
        .map(line -> new ReservationLine(
            line.productId(),
            line.quantity()
        ))
        .toList();

    //Reserve stock
    boolean reserved = stockService.reserve(reservationLines);

    if(reserved){
      log.info("Stock reserved for order {}", event.orderId());
      stockEventPublisher.publishStockReserved(
          new StockReserved(1, event.orderId()));
    } else {
      log.warn("Stock reservation failed for order {}", event.orderId());
      stockEventPublisher.publishStockRejected(
          new StockRejected(1, event.orderId(), "Insufficient stock"));
    }
  }

}
