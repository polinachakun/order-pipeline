package com.example.warehouseservice.service;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import com.example.warehouseservice.dto.StartOrderCommand;

public interface WarehouseService {
    void processNewOrder(OrderDto orderDto);

    void addStockAndRecheckPendingOrders(ItemDto itemDto);

    void compensateInventory(OrderStatusUpdateEventDto event);

    String handleStartOrderCommand(StartOrderCommand command);
}
