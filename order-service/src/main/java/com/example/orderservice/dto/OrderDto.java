package com.example.orderservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto implements AbstractDto {
    private String orderId;
    private LocalDate orderDate;
    private String deliveryLocation;
    private List<ItemDto> requestedItems;
    private String status;

}