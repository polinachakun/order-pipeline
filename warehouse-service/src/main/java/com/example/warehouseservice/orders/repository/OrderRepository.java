package com.example.warehouseservice.orders.repository;

import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(OrderDto orderDto);

    void updateStatus(String orderId, OrderStatus status);

    Optional<OrderDto> findById(String orderId);

    List<OrderDto> findAll();

    List<OrderDto> findByStatuses(List<OrderStatus> statuses);
}
