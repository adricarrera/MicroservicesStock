package com.adricarrera.orderservice.order.events;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void publishOrderCreated(OrderCreated event) {
    kafkaTemplate.send("orders.created", event.orderId().toString(), event);
  }
}
