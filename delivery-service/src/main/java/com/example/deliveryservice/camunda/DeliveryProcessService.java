package com.example.deliveryservice.camunda;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class DeliveryProcessService {

    private final ZeebeClient zeebeClient;

    public void triggerDeliveryCompletion(String orderId, boolean success) {
        try {
            Map<String, Object> variables = Map.of(
                    "deliverySuccessful", success,
                    "orderId", orderId
            );

            log.info("Manually triggering delivery completion for order: {}, success: {}", orderId, success);


            if (!success) {
                zeebeClient.newPublishMessageCommand()
                        .messageName("DeliveryFailed")
                        .correlationKey(orderId)
                        .variables(variables)
                        .send()
                        .join();
            }

        } catch (Exception e) {
            log.error("Failed to trigger delivery completion", e);
            throw new RuntimeException("Failed to trigger delivery completion: " + e.getMessage(), e);
        }
    }

}