package com.example.orderservice.consumer.events;

public class OrderStatusUpdateEvent {
    private String orderId;
    private String newStatus;

    public OrderStatusUpdateEvent(String orderId, String newStatus) {
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
