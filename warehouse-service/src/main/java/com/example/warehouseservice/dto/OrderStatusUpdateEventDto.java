package com.example.warehouseservice.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderStatusUpdateEventDto implements AbstractDto {

   private String orderId;
   private String status;


}
