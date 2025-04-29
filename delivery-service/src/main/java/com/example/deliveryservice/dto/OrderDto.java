package com.example.deliveryservice.dto;

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

    private List<ItemDto> items = new ArrayList<>();

    private String deliveryLocation;
}
