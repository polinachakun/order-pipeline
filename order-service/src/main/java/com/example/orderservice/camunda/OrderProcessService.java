package com.example.orderservice.camunda;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderStatusUpdateEventDto;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    public void onDeliveryStatus(OrderStatusUpdateEventDto event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("Ignoring null/invalid DeliveryStatusEvent: {}", event);
            return;
        }

        String orderId = event.getOrderId();
        String status = event.getStatus();


        log.info("Processing delivery status: orderId={}, status={}", orderId, status);
        if ("DELIVERED".equals(status)) {
            log.info("Publishing message: name=deliveryCompleted, correlationKey={}", orderId);
            try {
                log.info("Attempting to publish message: name=0b395884-003b-4491-bc45-a7723def7523, " +
                        "correlationKey={}", orderId);
                zeebeClient
                        .newPublishMessageCommand()
                        .messageName("deliveryCompleted")
                        .correlationKey(orderId)
                        .variables(Map.of(
                                "orderId", orderId,
                                "status", "DELIVERED"
                        ))
                        .send()
                        .join();
                log.info("Successfully published message");
                publishDeliveryCompletedMessage(orderId);
            } catch (Exception e) {
                log.error("Failed to publish message", e);
            }
        }
        log.info("Listener finished");
    }

    public void publishDeliveryCompletedMessage(String orderId) {
        log.info("Publishing delivery completed message for order: {}", orderId);

        // Try with dynamic message name
        try {
            zeebeClient
                    .newPublishMessageCommand()
                    .messageName("deliveryCompleted")
                    .correlationKey(orderId)
                    .variables(Map.of(
                            "orderId", orderId,
                            "deliveryStatus", "DELIVERED",
                            "messageReceived", true
                    ))
                    .send()
                    .join();
            log.info("Successfully published message with UUID name");
        } catch (Exception e) {
            log.warn("Failed to publish with UUID message name: {}", e.getMessage());
        }

        // Also try with the fixed message name
        try {
            zeebeClient
                    .newPublishMessageCommand()
                    .messageName("deliveryCompleted")  // Fixed name that should match BPMN
                    .correlationKey(orderId)
                    .variables(Map.of(
                            "orderId", orderId,
                            "deliveryStatus", "DELIVERED",
                            "messageReceived", true
                    ))
                    .send()
                    .join();
            log.info("Successfully published message with fixed name");
        } catch (Exception e) {
            log.warn("Failed to publish with fixed message name: {}", e.getMessage());
        }
    }

}