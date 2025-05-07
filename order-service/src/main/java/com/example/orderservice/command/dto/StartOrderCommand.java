package com.example.orderservice.command.dto;

import com.example.orderservice.dto.AbstractDto;
import com.example.orderservice.dto.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StartOrderCommand implements AbstractDto {

    private String orderId;

    private String deliveryLocation;

    private List<ItemDto> requestedItems;

    private String status;
}
