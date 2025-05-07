package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class Order {
    private String orderId;
    private LocalDate orderDate;
    private String deliveryLocation;
    private List<OrderItem> requestedItems;
    
    private String status = "CREATED";
    private Instant createdAt = Instant.now();

    @JsonCreator
    public Order(
            @JsonProperty("orderId")          String orderId,
            @JsonProperty("deliveryLocation") String deliveryLocation,
            @JsonProperty("requestedItems")   List<OrderItem> requestedItems
    ) {
        this.orderId          = (orderId != null) ? orderId : UUID.randomUUID().toString();
        this.orderDate        = LocalDate.now();
        this.deliveryLocation = deliveryLocation;
        this.requestedItems   = requestedItems;
    }

}
