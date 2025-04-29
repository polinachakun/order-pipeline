package com.example.dto;

public class ItemDto {
    private String itemId;
    private Integer quantity;

    public ItemDto() {
    }

    public ItemDto(String itemId, Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

