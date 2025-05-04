package com.example.deliveryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartDeliveryCommand {

    private String deliveryId;

    private String orderId;

    private String deliveryLocation;

    private String status;

}
