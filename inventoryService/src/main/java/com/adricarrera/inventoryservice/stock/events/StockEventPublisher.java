package com.adricarrera.inventoryservice.stock.events;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StockEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void publishStockReserved(StockReserved event) {
    kafkaTemplate.send(
        "stock.reserved",
        event.orderId().toString(),
        event);
  }

  public void publishStockRejected(StockRejected event) {
    kafkaTemplate.send(
        "stock.rejected",
        event.orderId().toString(),
        event
    );
  }
}
