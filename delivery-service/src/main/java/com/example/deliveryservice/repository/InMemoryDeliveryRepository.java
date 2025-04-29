package com.example.deliveryservice.repository;

import com.example.deliveryservice.dto.StartDeliveryCommand;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDeliveryRepository implements DeliveryRepository {

    private final Map<String, StartDeliveryCommand> deliveries = new ConcurrentHashMap<>();

    @Override
    public void save(StartDeliveryCommand startDeliveryCommand) {
        deliveries.put(String.valueOf(startDeliveryCommand.getDeliveryId()), startDeliveryCommand);
    }

    @Override
    public Optional<StartDeliveryCommand> findById(String deliveryId) {
        return Optional.ofNullable(deliveries.get(deliveryId));
    }

    @Override
    public Optional<StartDeliveryCommand> findByOrderId(String orderId) {
        return Optional.ofNullable(deliveries.get(orderId));
    }

    @Override
    public List<StartDeliveryCommand> findAll() {
        return new ArrayList<>(this.deliveries.values());
    }

}

