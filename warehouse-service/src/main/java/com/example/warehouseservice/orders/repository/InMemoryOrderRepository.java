package com.example.warehouseservice.orders.repository;

import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, OrderDto> orders = new ConcurrentHashMap<>();

    @Override
    public void save(OrderDto orderDto) {
        orders.put(orderDto.getOrderId(), orderDto);
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        OrderDto existingOrder = orders.get(orderId);
        if (existingOrder != null) {
            existingOrder.setStatus(status);
        }
    }

    @Override
    public Optional<OrderDto> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<OrderDto> findAll() {
        return new ArrayList<>(this.orders.values());
    }

    @Override
    public List<OrderDto> findByStatuses(List<OrderStatus> statuses) {
        return orders.values().stream()
                .filter(order -> statuses.contains(order.getStatus()))
                .toList();
    }
}

