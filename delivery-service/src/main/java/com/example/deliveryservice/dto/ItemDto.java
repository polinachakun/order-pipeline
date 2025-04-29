package com.example.deliveryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ItemDto implements AbstractDto {

    private String itemId;

    private Integer quantity;

}

