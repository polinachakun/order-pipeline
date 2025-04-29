package com.example.warehouseservice.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class OrderDto implements AbstractDto {

    private String orderId;

    private List<ItemDto> requestedItems = new ArrayList<>(); //todo annotate or create a separte OrderDto for listener

    private OrderStatus status;

    private List<ItemDto> missingItems = new ArrayList<>();

    private String deliveryLocation;

}
