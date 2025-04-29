package com.example.producer.events;

import com.example.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class OrderCreatedEvent {
    private String orderId;
    private String deliveryLocation;
    private LocalDate orderDate;
    private List<OrderItem> requestedItems;
    private String status;
    private Instant timestamp;

    public OrderCreatedEvent(
            String orderId,
            String deliveryLocation,
            LocalDate orderDate,
            List<OrderItem> requestedItems,
            String status,
            Instant timestamp
    ) {
        this.orderId = orderId;
        this.deliveryLocation = deliveryLocation;
        this.orderDate = orderDate;
        this.requestedItems = requestedItems;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("deliveryLocation")
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    @JsonProperty("requestedItems")
    public List<OrderItem> getRequestedItems() {
        return requestedItems;
    }

    public String getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
