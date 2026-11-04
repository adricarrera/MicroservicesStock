package com.adricarrera.orderservice.order.events;


import com.adricarrera.orderservice.order.application.OrderService;
import com.adricarrera.orderservice.order.domain.Order;
import com.adricarrera.orderservice.order.domain.OrderNotFoundException;
import com.adricarrera.orderservice.order.domain.OrderStatus;
import com.adricarrera.orderservice.order.infrastructure.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StockResultListener {

  private final OrderService orderService;
  private final OrderEventPublisher orderEventPublisher;
  private final OrderRepository orderRepository;

  //Update order status to "CONFIRMED"
  @KafkaListener(topics = "stock.reserved")
  public void onStockReserved(StockReserved event){
    Order order = orderRepository.findById(event.orderId())
        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + event.orderId() + " not found"));

    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);

    log.info("Order {} CONFIRMED", event.orderId());
  }

  @KafkaListener(topics = "stock.rejected")
  public void onStockRejected(StockRejected event){
    Order order = orderRepository.findById(event.orderId())
        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + event.orderId() + " not found"));

    order.setStatus(OrderStatus.REJECTED);
    orderRepository.save(order);

    log.info("Order {} REJECTED. Reason: {}", event.orderId(), event.reason());
  }
}
