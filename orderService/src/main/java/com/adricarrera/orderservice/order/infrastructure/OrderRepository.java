package com.adricarrera.orderservice.order.infrastructure;

import com.adricarrera.orderservice.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
