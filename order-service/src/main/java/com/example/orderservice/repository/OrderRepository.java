package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {
    private final Map<String, Order> storage = new ConcurrentHashMap<>();

    public Order save(Order order) {
        storage.put(order.getOrderId(), order);
        return order;
    }

    public Order findById(String orderId) {
        return storage.get(orderId);
    }

    public Order updateStatus(String orderId, String newStatus) {
        Order order = storage.get(orderId);
        if (order != null) {
            order.setStatus(newStatus);
        }
        return order;
    }
}
