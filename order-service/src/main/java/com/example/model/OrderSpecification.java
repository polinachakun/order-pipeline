package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderSpecification {
    private int quantity;
    private double weight;
    private double price;

    @JsonCreator
    public OrderSpecification(
            @JsonProperty("quantity") int quantity,
            @JsonProperty("weight") double weight,
            @JsonProperty("price") double price) {
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
    }

    public int getQuantity() { return quantity; }
    public double getWeight() { return weight; }
    public double getPrice() { return price; }
}
