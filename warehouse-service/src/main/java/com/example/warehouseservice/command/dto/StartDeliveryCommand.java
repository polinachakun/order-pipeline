package com.example.warehouseservice.command.dto;

import com.example.warehouseservice.dto.AbstractDto;
import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StartDeliveryCommand implements AbstractDto {

    private String orderId;

//    private String orderId;
//
//    private List<ItemDto> items;
//
    private String deliveryLocation;
}
