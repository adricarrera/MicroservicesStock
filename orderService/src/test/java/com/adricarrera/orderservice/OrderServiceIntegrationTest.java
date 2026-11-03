package com.adricarrera.orderservice;

import com.adricarrera.orderservice.order.api.CreateOrderRequest;
import com.adricarrera.orderservice.order.api.OrderResponse;
import com.adricarrera.orderservice.order.application.OrderService;
import com.adricarrera.orderservice.order.domain.OrderNotFoundException;
import com.adricarrera.orderservice.order.domain.OrderStatus;
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

@SpringBootTest
@Testcontainers
public class OrderServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

  @Autowired
  OrderService orderService;

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
}
