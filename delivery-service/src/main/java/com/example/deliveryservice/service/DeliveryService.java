package com.example.deliveryservice.service;

import com.example.deliveryservice.dto.DeliveryStatus;
import com.example.deliveryservice.dto.StartDeliveryCommand;

import java.util.List;

public interface DeliveryService {

    String handleStartDeliveryCommand(StartDeliveryCommand command);

    List<StartDeliveryCommand> findAll();

    void publishDeliveryCompletedStatus(String orderId, DeliveryStatus status);


    void publishStatusUpdate(String orderId, DeliveryStatus status);

    void publishDeliveryFailedStatus(String orderId);

    void publishDeliveryCancelledStatus(String orderId);


}
