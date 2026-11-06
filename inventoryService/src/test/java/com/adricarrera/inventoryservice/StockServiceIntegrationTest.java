package com.adricarrera.inventoryservice;

import com.adricarrera.inventoryservice.stock.application.ReservationLine;
import com.adricarrera.inventoryservice.stock.application.StockService;
import com.adricarrera.inventoryservice.stock.domain.Product;
import com.adricarrera.inventoryservice.stock.events.StockEventPublisher;
import com.adricarrera.inventoryservice.stock.events.StockRejected;
import com.adricarrera.inventoryservice.stock.events.StockReserved;
import com.adricarrera.inventoryservice.stock.infrastructure.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;


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

  @MockitoBean // Mock the StockEventPublisher bean to avoid sending real Kafka messages during tests
  StockEventPublisher stockEventPublisher;

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

    UUID orderId = UUID.randomUUID();
    stockService.reserve(lines, orderId);
    verify(stockEventPublisher,atLeastOnce()).publishStockReserved(new StockReserved(1, orderId));
    verify(stockEventPublisher, never()).publishStockRejected(any(StockRejected.class));
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(8);
    assertThat(productRepository.findById(product2.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(2);
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
        new ReservationLine(product1.getId(), 5),
        new ReservationLine(product2.getId(), 10)
    );

    UUID orderId = UUID.randomUUID();
    stockService.reserve(lines, orderId);
    verify(stockEventPublisher,never()).publishStockReserved(any(StockReserved.class));
    verify(stockEventPublisher, atLeastOnce()).publishStockRejected(new StockRejected(1, orderId, "Insufficient stock"));
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(10);
    assertThat(productRepository.findById(product2.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(5);
  }

  @Test
  void doesNotSellMoreThanAvailableStockWhenReservingConcurrently() throws Exception {
    int nThreads = 10;
    int stock = 5;

    Product product = Product.builder()
        .name("Product 1")
        .availableQuantity(stock)
        .build();

    productRepository.save(product);

    List<ReservationLine> lines = List.of(new ReservationLine(product.getId(), 1));

    ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    CountDownLatch startSignal = new CountDownLatch(1);

    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < nThreads; i++) {
      futures.add(executor.submit(() -> {
        try {
          startSignal.await();                              // línea de salida
          stockService.reserve(lines, UUID.randomUUID());   // un pedido distinto cada vez        } catch (Exception e) {
        } catch (Exception e) {
          // si la reserva falla con una excepción, no se publica ningún evento
          // y las comprobaciones de abajo lo detectarán
        }
      }));
    }

    startSignal.countDown();

    for (Future<?> future : futures) {
      future.get();                                         // esperar a que terminen todos
    }
    executor.shutdown();

    verify(stockEventPublisher, times(stock)).publishStockReserved(any());
    verify(stockEventPublisher, times(nThreads - stock )).publishStockRejected(any());
    assertThat(productRepository.findById(product.getId()).orElseThrow().getAvailableQuantity()).isZero();
  }

  @Test
  void productNotExists(){
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
        new ReservationLine(product1.getId(), 5),
        new ReservationLine(product2.getId(), 5),
        new ReservationLine(UUID.randomUUID(), 1)
    );

    UUID orderId = UUID.randomUUID();
    stockService.reserve(lines, orderId);
    verify(stockEventPublisher,never()).publishStockReserved(any(StockReserved.class));
    verify(stockEventPublisher, atLeastOnce()).publishStockRejected(new StockRejected(1, orderId, "Insufficient stock"));
    assertThat(productRepository.findById(product1.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(10);
    assertThat(productRepository.findById(product2.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(5);
  }
}
