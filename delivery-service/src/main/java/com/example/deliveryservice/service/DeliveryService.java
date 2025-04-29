package com.example.deliveryservice.service;

import com.example.deliveryservice.dto.StartDeliveryCommand;

import java.util.List;

public interface DeliveryService {

    String handleStartDeliveryCommand(StartDeliveryCommand command);

    List<StartDeliveryCommand> findAll();

}
