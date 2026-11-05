package com.adricarrera.orderservice.order.events;


import com.adricarrera.orderservice.order.application.OrderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StockResultListener {

  OrderService orderService;

  //Update order status to "CONFIRMED"
  @KafkaListener(topics = "stock.reserved")
  public void onStockReserved(StockReserved event){
   orderService.stockReserveConfirmed(event.orderId());
  }

  @KafkaListener(topics = "stock.rejected")
  public void onStockRejected(StockRejected event){
    orderService.stockReserveRejected(event.orderId(), event.reason());
  }
}
