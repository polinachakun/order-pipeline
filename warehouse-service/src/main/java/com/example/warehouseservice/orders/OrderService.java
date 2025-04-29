package com.example.warehouseservice.orders;

import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatus;

import java.util.List;

public interface OrderService {

    void initializeNewOrder(OrderDto orderDto);

    void markOrderAsFullyAvailable(String orderId);

    void markOrderAsPartiallyAvailable(String orderId, List<String> missingSkus);

    void markOrderAsReadyForPicking(String orderId);

    List<OrderDto> findAll();

    List<OrderDto> getOrdersByStatuses(OrderStatus... statuses);

    void markOrderAsCancelled(String orderId);
}
