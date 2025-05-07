package com.example.warehouseservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartOrderCommand {

    private String deliveryId;

    private String orderId;

    private String deliveryLocation;

    private String status;

}
