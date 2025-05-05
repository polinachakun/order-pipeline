package com.example.orderservice.camunda;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.dto.OrderStatusUpdateEventDto;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class OrderProcessService {

    private final ZeebeClient zeebeClient;

    private final OrderService orderService;


    private final ObjectMapper objectMapper;

    private final Set<String> processedEvents = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public String startOrderSaga(OrderDto orderDto) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("items", orderDto.getRequestedItems());
        variables.put("deliveryLocation", orderDto.getDeliveryLocation());

        ProcessInstanceEvent processInstance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("OrderSaga")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("Started Order Saga process instance: {}", processInstance.getProcessInstanceKey());

        return String.valueOf(processInstance.getProcessInstanceKey());
    }

//    public void notifyDeliveryCompleted(String orderId) {
//        zeebeClient.newPublishMessageCommand()
//                .messageName("DeliveryCompleted")    // must exactly match the BPMN element
//                .correlationKey(orderId)             // matches the process variable “orderId”
//                .variables(Map.of(
//                        "orderId", orderId,
//                        "deliveryStatus", "DELIVERED"
//                ))
//                .send()
//                .join();
//
//        log.info("➡️  Correlated Zeebe message 'DeliveryCompleted' for order={}", orderId);
//    }

    @KafkaListener(
            topics = "${kafka.ordersStatusUpdate.topic}",
            groupId = "${kafka.ordersStatusUpdate.camunda-group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void onDeliveryStatus(OrderStatusUpdateEventDto evt) {

        if (evt == null || evt.getOrderId() == null) {
            log.warn("Ignoring null/invalid DeliveryStatusEvent: {}", evt);
            return;
        }

        String orderId = evt.getOrderId();
        String status = evt.getStatus();

        String eventKey = orderId + "-" + status + "-" + System.currentTimeMillis()/1000;

        if (!processedEvents.add(eventKey)) {
            log.debug("Event already processed: {}", eventKey);
            return;
        }

        log.info("Processing delivery status: orderId={}, status={}", orderId, status);

        try {
            // Update order status in the database
            orderService.updateStatus(orderId, status);

            // Send appropriate message to Camunda based on status
            if ("DELIVERED".equals(status)) {
                zeebeClient.newPublishMessageCommand()
                        .messageName("deliveryCompleted")
                        .correlationKey(orderId)
                        .variables(Map.of(
                                "orderId", orderId,
                                "deliveryStatus", "DELIVERED"
                        ))
                        .send()
                        .join();

                log.info("Successfully published deliveryCompleted message for order: {}", orderId);
            } else if ("DELIVERY_FAILED".equals(status)) {
                zeebeClient.newPublishMessageCommand()
                        .messageName("deliveryFailed")
                        .correlationKey(orderId)
                        .variables(Map.of(
                                "orderId", orderId,
                                "deliveryStatus", "DELIVERY_FAILED"
                        ))
                        .send()
                        .join();

                log.info("Published deliveryFailed message for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process delivery status for order: {}", orderId, e);
            processedEvents.remove(eventKey);
            throw e;
        }
    }
}