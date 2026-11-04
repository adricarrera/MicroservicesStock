package com.adricarrera.inventoryservice.stock.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreatedListener {

  @KafkaListener(topics = "orders.created")
  public void onOrderCreated(OrderCreated event){
    log.info("Received OrderCreated event: {}", event);
  }
}
