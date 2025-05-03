package com.example.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String orderId;
    private LocalDate orderDate;
    private String deliveryLocation;
    private List<ItemDto> requestedItems;
    private String status;

}