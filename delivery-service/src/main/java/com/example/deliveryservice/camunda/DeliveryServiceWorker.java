package com.example.deliveryservice.camunda;

import com.example.deliveryservice.dto.DeliveryStatus;
import com.example.deliveryservice.dto.OrderStatusUpdateEventDto;
import com.example.deliveryservice.dto.StartDeliveryCommand;
import com.example.deliveryservice.repository.DeliveryRepository;
import com.example.deliveryservice.service.DeliveryService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.deliveryservice.dto.DeliveryStatus.DELIVERED;

@Slf4j
@Component
@AllArgsConstructor
public class DeliveryServiceWorker {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @JobWorker(type = "startDelivery", autoComplete = true)
    public void startDelivery(
            @Variable String orderId,
            @Variable String deliveryLocation
    ) {
        log.info("[Camunda Worker] startDelivery: order={}, location={}", orderId, deliveryLocation);
        try {

            String deliveryId = UUID.randomUUID().toString();
            String status = DeliveryStatus.DELIVERY_STARTED.name();
            StartDeliveryCommand command = new StartDeliveryCommand(
                    deliveryId,
                    orderId,
                    deliveryLocation,
                    status
            );
            deliveryService.handleStartDeliveryCommand(command);
            // autoComplete=true will complete this task automatically
        } catch (Exception e) {
            log.error("Error in startDelivery for order {}", orderId, e);
            throw new RuntimeException(e);
        }
    }

    @JobWorker(type = "completeDelivery", autoComplete = false)
    public void completeDelivery(
            JobClient client,
            ActivatedJob job,
            @Variable String orderId,
            @Variable String deliveryLocation
    ) {
        log.info("[Camunda Worker] completeDelivery: order={}, location={}", orderId, deliveryLocation);
        try {
            if (deliveryLocation == null || deliveryLocation.toLowerCase().contains("invalid")
                    || deliveryLocation.isBlank()) {
                log.warn("Invalid delivery location for order {}: {}", orderId, deliveryLocation);
                // publish failure event and throw BPMN error
                deliveryService.publishDeliveryFailedStatus(orderId);

                client.newThrowErrorCommand(job.getKey())
                        .errorCode("DELIVERY_FAILED")
                        .errorMessage("Invalid delivery location")
                        .send()
                        .join();
            } else {
                // good address: publish delivered event
                deliveryService.publishDeliveryCompletedStatus(orderId, DELIVERED);
                client.newCompleteCommand(job.getKey())
                        .send()
                        .join();
                log.info("completeDelivery: order {} marked delivered and job completed", orderId);
            }
        } catch (Exception e) {
            log.error("Exception in completeDelivery for order {}", orderId, e);
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("DELIVERY_ERROR")
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }

    @JobWorker(type = "cancelDelivery", autoComplete = true)
    public void cancelDelivery(@Variable String orderId) {
        log.info("[Camunda Worker] cancelDelivery: order={}", orderId);
        deliveryService.publishDeliveryCancelledStatus(orderId);
    }
}