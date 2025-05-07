package com.example.orderservice.orchestration;

import com.example.orderservice.dto.OrderDto;

public interface OrchestrationStrategy {
    void startOrderProcess(OrderDto orderRequest);
    void cancelOrder(String orderId);
    OrchestrationMode getMode();
}

