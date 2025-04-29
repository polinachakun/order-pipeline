package com.example.service;

import com.example.dto.OrderDto;
import com.example.model.Order;

public interface OrderService {
    Order saveOrder(Order order);
    Order findById(String orderId);
    Order updateStatus(String orderId, String status);
    void sendOrderToWarehouse(Order order);
    OrderDto processOrder(OrderDto orderDto);
}