package com.adricarrera.orderservice.order.application;

import com.adricarrera.orderservice.order.api.CreateOrderRequest;
import com.adricarrera.orderservice.order.api.OrderResponse;
import com.adricarrera.orderservice.order.domain.Order;
import com.adricarrera.orderservice.order.domain.OrderLine;
import com.adricarrera.orderservice.order.domain.OrderNotFoundException;
import com.adricarrera.orderservice.order.domain.OrderStatus;
import com.adricarrera.orderservice.order.events.OrderCreated;
import com.adricarrera.orderservice.order.events.OrderEventPublisher;
import com.adricarrera.orderservice.order.infrastructure.OrderRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderEventPublisher orderEventPublisher;

  @Transactional
  public OrderResponse createOrder(final CreateOrderRequest request) {
    Order order = Order.builder()
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .build();

    request.lines().forEach(lineRequest ->
      order.addLine(OrderLine.builder()
          .productId(lineRequest.productId())
          .quantity(lineRequest.quantity())
          .build()));

    Order orderSaved = orderRepository.save(order);

    orderEventPublisher.publishOrderCreated(new OrderCreated(
        1,
        orderSaved.getId(),
        orderSaved.getOrderLines().stream()
            .map(line -> new OrderCreated.Line(line.getProductId(), line.getQuantity()))
            .toList()
    ));

    return toResponse(orderSaved);
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrder(final UUID id){
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found" ));
    return toResponse(order);
  }


  //CONVERT ORDER INTO ORDER RESPONSE
  private static OrderResponse toResponse(final Order order) {
    return OrderResponse.builder()
        .id(order.getId())
        .createdAt(order.getCreatedAt())
        .status(order.getStatus())
        .lines(order.getOrderLines().stream()
            .map(line -> new OrderResponse.OrderLineResponse(line.getProductId(), line.getQuantity()))
            .toList())
        .build();
  }
}
