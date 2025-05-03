package com.example.service;

import com.example.dto.OrderDto;
import com.example.model.Order;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto findById(String orderId);
    OrderDto updateStatus(String orderId, String newStatus);
}
