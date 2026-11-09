package com.adricarrera.notificationservice.notification.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockResultListener {

  @KafkaListener(topics = "stock.reserved", properties = "spring.json.value.default.type=com.adricarrera.notificationservice.notification.events.StockReserved")
  public void onStockReserved(StockReserved event){
    log.info("NOTIFICATION: order {} confirmed, stock reserved", event.orderId());
  }

  @KafkaListener(topics = "stock.rejected", properties = "spring.json.value.default.type=com.adricarrera.notificationservice.notification.events.StockRejected")
  public void onStockRejected(StockRejected event){
    log.warn("NOTIFICATION: order {} rejected: {}", event.orderId(), event.reason());
  }


}
