package com.example.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartDeliveryCommand {

    private String deliveryId;

    private String orderId;

    private String deliveryLocation;

    private String status;

}
