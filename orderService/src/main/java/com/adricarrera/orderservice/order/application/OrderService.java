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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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

    Map<UUID, Integer> groupedLines = request.lines().stream()
        .collect(Collectors.groupingBy(
            CreateOrderRequest.OrderLineRequest::productId,
            Collectors.summingInt(
                CreateOrderRequest.OrderLineRequest::quantity
            )
        ));

    groupedLines.forEach((productId, quantity) ->
        order.addLine(OrderLine.builder()
            .productId(productId)
            .quantity(quantity)
            .build())
    );

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

  @Transactional
  public void confirmOrder(UUID orderId){
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    if(order.confirm()) {
      log.info("Order {} CONFIRMED", orderId);
    }else{
      logWarnOrderAlreadyInState(orderId, order);
    }
  }

  @Transactional
  public void rejectOrder(UUID orderId, String reason){
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    if(order.reject()) {
      log.info("Order {} REJECTED. Reason: {}", orderId, reason);
    }else{
      logWarnOrderAlreadyInState(orderId, order);
    }
  }

  private static void logWarnOrderAlreadyInState(UUID orderId, Order order) {
    log.warn("Order {} is already in state {}", orderId, order.getStatus());
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
