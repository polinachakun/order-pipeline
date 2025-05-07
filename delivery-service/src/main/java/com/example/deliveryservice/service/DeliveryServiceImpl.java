package com.example.deliveryservice.service;

import com.example.deliveryservice.dto.DeliveryStatus;
import com.example.deliveryservice.dto.OrderStatusUpdateEventDto;
import com.example.deliveryservice.dto.StartDeliveryCommand;
import com.example.deliveryservice.publisher.DeliveryEventPublisher;
import com.example.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.deliveryservice.dto.DeliveryStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryEventPublisher eventPublisher;
    private final DeliveryRepository deliveryRepository;

    @Override
    public String handleStartDeliveryCommand(StartDeliveryCommand command) {
        if (command == null || command.getOrderId() == null) {
            log.error("Cannot start delivery for null command or command with null orderId");
            return null;
        }

        String deliveryId = UUID.randomUUID().toString();
        command.setDeliveryId(deliveryId);
        command.setStatus(String.valueOf(DELIVERY_STARTED));

        log.info("Starting delivery for order {}, deliveryId={}", command.getOrderId(), deliveryId);
        deliveryRepository.save(command);

        publishStatusUpdate(command.getOrderId(), DELIVERY_STARTED);
        deliveryStatusUpdate();

        return deliveryId;
    }

//    @Scheduled(fixedRate = 5000)
    public void deliveryStatusUpdate() {
        List<StartDeliveryCommand> deliveries = findAll();

        if (deliveries == null || deliveries.isEmpty()) {
            return;
        }

        for (StartDeliveryCommand command : deliveries) {
            if (command == null || command.getOrderId() == null ||
                command.getStatus() == null || !DELIVERY_STARTED.name().equalsIgnoreCase(command.getStatus())) {
                continue;
            }

            try {
                if (command.getDeliveryLocation() == null ||
                    command.getDeliveryLocation().toLowerCase().contains("invalid") ||
                    command.getDeliveryLocation().isEmpty()) {
                    throw new RuntimeException("Invalid delivery address");
                }

                publishStatusUpdate(command.getOrderId(), DELIVERED);
                log.info("Delivery completed for order {}, deliveryId={}", command.getOrderId(), command.getDeliveryId());

                command.setStatus(String.valueOf(DELIVERED));
                deliveryRepository.save(command);

            } catch (Exception ex) {
                log.error("Delivery failed for order {}, reason: {}", command.getOrderId(), ex.getMessage());
                publishDeliveryFailedStatus(command.getOrderId());

                command.setStatus(DELIVERY_FAILED.name());
                deliveryRepository.save(command);
            }
        }
    }


    @Override
    public List<StartDeliveryCommand> findAll() {
        return deliveryRepository.findAll();
    }

    @Override
    public void publishDeliveryCompletedStatus(String orderId, DeliveryStatus status) {
        if (orderId == null) {
            log.error("Cannot publish delivery failed status update with null orderId");
            return;
        }
        OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, DeliveryStatus.DELIVERED.name());
        eventPublisher.publishCompletedDeliveryOrderStatusUpdate(event);

    }

    @Override
    public void publishStatusUpdate(String orderId, DeliveryStatus status) {
        if (orderId == null || status == null) {
            log.error("Cannot publish status update with null orderId or status");
            return;
        }
        OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, status.name());
        eventPublisher.publishOrderStatusUpdate(event);
    }

    @Override
    public void publishDeliveryFailedStatus(String orderId) {
        if (orderId == null) {
            log.error("Cannot publish delivery failed status update with null orderId");
            return;
        }
        OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, DeliveryStatus.DELIVERY_FAILED.name());
        eventPublisher.publishDeliveryFailedOrderStatusUpdate(event);
    }

    @Override
    public void publishDeliveryCancelledStatus(String orderId) {
        if (orderId == null) {
            log.error("Cannot publish delivery failed status update with null orderId");
            return;
        }
        OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, DELIVERY_CANCELLED.name());
        eventPublisher.publishDeliveryFailedOrderStatusUpdate(event);
    }

}
