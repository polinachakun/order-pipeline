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

    /**
     * Manually trigger delivery completion (for testing)
     */
    public void triggerDeliveryCompletion(String orderId, boolean success) {
        try {
            Map<String, Object> variables = Map.of(
                    "deliverySuccessful", success,
                    "orderId", orderId
            );

            // In real scenario, this would be handled by the scheduled job
            log.info("Manually triggering delivery completion for order: {}, success: {}", orderId, success);


            if (!success) {
                // Trigger delivery failure event
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

    /**
     * Get active jobs for monitoring
     */
    public void checkActiveDeliveries() {
        // This is more for monitoring purposes
        log.info("Checking active deliveries in Camunda");
        // Implementation would depend on your monitoring needs
    }
}