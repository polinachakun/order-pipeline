package com.example.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderDto {
    private String orderId;
    private List<ItemDto> requestedItems = new ArrayList<>();
    private String status;
    private List<ItemDto> missingItems = new ArrayList<>();
    private String deliveryLocation;

    public OrderDto() {
    }

    public OrderDto(String orderId, List<ItemDto> requestedItems, String status, List<ItemDto> missingItems, String deliveryLocation) {
        this.orderId = orderId;
        this.requestedItems = requestedItems;
        this.status = status;
        this.missingItems = missingItems;
        this.deliveryLocation = deliveryLocation;
    }

    public static OrderDtoBuilder builder() {
        return new OrderDtoBuilder();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ItemDto> getRequestedItems() {
        return requestedItems;
    }

    public void setRequestedItems(List<ItemDto> requestedItems) {
        this.requestedItems = requestedItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemDto> getMissingItems() {
        return missingItems;
    }

    public void setMissingItems(List<ItemDto> missingItems) {
        this.missingItems = missingItems;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public static class OrderDtoBuilder {
       private String orderId;
        private List<ItemDto> requestedItems = new ArrayList<>();
        private String status;
        private List<ItemDto> missingItems = new ArrayList<>();
        private String deliveryLocation;

        OrderDtoBuilder() {
        }

        public OrderDtoBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderDtoBuilder requestedItems(List<ItemDto> requestedItems) {
            this.requestedItems = requestedItems;
            return this;
        }

        public OrderDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public OrderDtoBuilder missingItems(List<ItemDto> missingItems) {
            this.missingItems = missingItems;
            return this;
        }

        public OrderDtoBuilder deliveryLocation(String deliveryLocation) {
            this.deliveryLocation = deliveryLocation;
            return this;
        }

        public OrderDto build() {
            return new OrderDto(orderId, requestedItems, status, missingItems, deliveryLocation);
        }
    }
}