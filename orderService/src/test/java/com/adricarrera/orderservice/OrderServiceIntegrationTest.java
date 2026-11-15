package com.adricarrera.orderservice;

import com.adricarrera.orderservice.order.api.CreateOrderRequest;
import com.adricarrera.orderservice.order.api.OrderResponse;
import com.adricarrera.orderservice.order.application.OrderService;
import com.adricarrera.orderservice.order.domain.Order;
import com.adricarrera.orderservice.order.domain.OrderNotFoundException;
import com.adricarrera.orderservice.order.domain.OrderStatus;
import com.adricarrera.orderservice.order.events.StockRejected;
import com.adricarrera.orderservice.order.events.StockReserved;
import com.adricarrera.orderservice.order.events.StockResultListener;
import com.adricarrera.orderservice.order.infrastructure.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class OrderServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

  @Autowired
  OrderService orderService;

  @Autowired
  OrderRepository orderRepository;

  @Autowired
  StockResultListener stockResultListener;

  @Test
  void createsAndRetrievesOrder(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    OrderResponse found = orderService.getOrder(created.id());

    assertThat(created.id()).isNotNull();
    assertThat(found.status()).isEqualTo(OrderStatus.PENDING);
    assertThat(found.lines()).hasSize(1);
    assertThat(found.lines().getFirst().quantity()).isEqualTo(2);
  }

  @Test
  void shouldThrowOrderNotFoundException(){
    UUID randomId = UUID.randomUUID();

    assertThatThrownBy(() -> orderService.getOrder(randomId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void shouldReserveStockCorrectly(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    StockReserved stockReserved = new StockReserved(1, created.id());
    stockResultListener.onStockReserved(stockReserved);

    assertThat(orderRepository.findById(created.id())
        .orElseThrow()
        .getStatus())
        .isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  void shouldRejectReservation(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    StockRejected stockRejected = new StockRejected(1, created.id(), "Not enough stock");
    stockResultListener.onStockRejected(stockRejected);

    assertThat(orderRepository.findById(created.id())
        .orElseThrow()
        .getStatus())
        .isEqualTo(OrderStatus.REJECTED);
  }

  @Test
  void shouldNotConfirmOrderAgain(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    StockReserved stockReserved = new StockReserved(1, created.id());

    stockResultListener.onStockReserved(stockReserved);
    stockResultListener.onStockReserved(stockReserved);

    assertThat(orderRepository.findById(created.id())
        .orElseThrow()
        .getStatus())
        .isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  void shouldNotRejectOrderConfirmed(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    StockReserved stockReserved = new StockReserved(1, created.id());
    StockRejected stockRejected = new StockRejected(1, created.id(),"Not enough stock");

    stockResultListener.onStockReserved(stockReserved);
    stockResultListener.onStockRejected(stockRejected);

    assertThat(orderRepository.findById(created.id())
        .orElseThrow()
        .getStatus())
        .isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  void shouldNotConfirmOrderRejected(){
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(UUID.randomUUID(), 2)));

    OrderResponse created = orderService.createOrder(request);
    StockRejected stockRejected = new StockRejected(1, created.id(),"Not enough stock");
    StockReserved stockReserved = new StockReserved(1, created.id());

    stockResultListener.onStockRejected(stockRejected);
    stockResultListener.onStockReserved(stockReserved);

    assertThat(orderRepository.findById(created.id())
        .orElseThrow()
        .getStatus())
        .isEqualTo(OrderStatus.REJECTED);
  }

  @Test
  void mergesLinesForTheSameProduct() {
    UUID productId = UUID.randomUUID();
    var request = new CreateOrderRequest(List.of(
        new CreateOrderRequest.OrderLineRequest(productId, 2),
        new CreateOrderRequest.OrderLineRequest(productId, 2)
    ));

    OrderResponse created = orderService.createOrder(request);
    OrderResponse saved = orderService.getOrder(created.id());

    assertThat(saved.lines())
        .containsExactly(new OrderResponse.OrderLineResponse(productId, 4));
  }

  @Test
  void shouldNotConfirmOrderTwice() {
    Order order = Order.builder()
        .status(OrderStatus.PENDING)
        .build();

    assertThat(order.confirm()).isTrue();

    assertThat(order.confirm()).isFalse();

    assertThat(order.getStatus())
        .isEqualTo(OrderStatus.CONFIRMED);
  }
}
