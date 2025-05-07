package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;

import java.util.Optional;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    Optional<OrderDto> findById(String orderId);
    OrderDto updateStatus(String orderId, String newStatus);

}
