package com.example.deliveryservice.repository;

import com.example.deliveryservice.dto.StartDeliveryCommand;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
    void save(StartDeliveryCommand startDeliveryCommand);

    Optional<StartDeliveryCommand> findById(String deliveryId);

    Optional<StartDeliveryCommand> findByOrderId(String orderId);

    List<StartDeliveryCommand> findAll();

}
