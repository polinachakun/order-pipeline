package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Order {
    private String orderId;
    private LocalDate orderDate;
    private String deliveryLocation;
    private List<OrderItem> requestedItems;
    private String status;
    private Instant createdAt;

    @JsonCreator
    public Order(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderDate") LocalDate orderDate,
            @JsonProperty("deliveryLocation") String deliveryLocation,
            @JsonProperty("requestedItems") List<OrderItem> requestedItems
    ) {
        this.orderId = (orderId != null) ? orderId : UUID.randomUUID().toString();
        this.orderDate = (orderDate != null) ? orderDate : LocalDate.now();
        this.deliveryLocation = deliveryLocation;
        this.requestedItems = requestedItems;
        this.status = "CREATED";
        this.createdAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getLocation() {
        return deliveryLocation;
    }

    @JsonProperty("deliveryLocation")
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    @JsonProperty("requestedItems")
    public List<OrderItem> getItems() {
        return requestedItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
