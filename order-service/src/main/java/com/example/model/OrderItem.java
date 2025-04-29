package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItem {
    private String itemId;
    private int quantity;

    @JsonCreator
    public OrderItem(
            @JsonProperty("itemId") String itemId,
            @JsonProperty("quantity") int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
