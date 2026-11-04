package com.adricarrera.inventoryservice;

import com.adricarrera.inventoryservice.stock.application.ReservationLine;
import com.adricarrera.inventoryservice.stock.application.StockService;
import com.adricarrera.inventoryservice.stock.domain.Product;
import com.adricarrera.inventoryservice.stock.infrastructure.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Testcontainers
public class StockServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

  @Autowired
  StockService stockService;

  @Autowired
  ProductRepository productRepository;

  @Test
  void reserveStockSuccessfully(){
    Product product1 = Product.builder()
        .name("Product 1")
        .availableQuantity(10)
        .build();

    Product product2 = Product.builder()
        .name("Product 2")
        .availableQuantity(5)
        .build();

    productRepository.saveAll(List.of(product1, product2));

    List<ReservationLine> lines = List.of(
        new ReservationLine(product1.getId(), 2),
        new ReservationLine(product2.getId(), 3)
    );

    assertThat(stockService.reserve(lines)).isTrue();
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(8);
    assertThat(productRepository.findById(product2.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(2
    );
  }

  @Test
  void notEnoughStock(){
    Product product1 = Product.builder()
        .name("Product 1")
        .availableQuantity(10)
        .build();

    Product product2 = Product.builder()
        .name("Product 2")
        .availableQuantity(5)
        .build();

    productRepository.saveAll(List.of(product1, product2));

    List<ReservationLine> lines = List.of(
        new ReservationLine(product1.getId(), 2),
        new ReservationLine(product2.getId(), 6)
    );

    assertThat(stockService.reserve(lines)).isFalse();
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(10);
    assertThat(productRepository.findById(product2.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(5);
  }

  @Test
  void productNotExists(){
    Product product1 = Product.builder()
        .name("Product 1")
        .availableQuantity(10)
        .build();

    productRepository.save(product1);

    List<ReservationLine> lines = List.of(
        new ReservationLine(UUID.randomUUID(), 2)
    );

    assertThat(stockService.reserve(lines)).isFalse();
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(10);
  }
}
