package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDto implements AbstractDto {
    private String itemId;
    private Integer quantity;

}

