# ADR: Error Scenario Handling for Delivery Failures

**Status:** Accepted  
**Date:** 2025-03-23

## Context
In our order processing system, the Delivery Service is responsible for managing delivery attempts. In cases where a delivery fails—due, for example, to an invalid address or other errors—the system must execute compensating actions. Specifically:

- The Delivery Service publishes an event indicating the failure.
- The Warehouse Service consumes this event, restores the inventory for the undelivered items, and then publishes a follow-up event.
- The Order Service receives the cancellation event, updates the order status, and notifies the customer of the delivery failure.

This error scenario sets the stage for future enhancements where advanced resilience patterns (stateful retry, human intervention, or saga patterns) may be integrated, possibly with Camunda BPM.

## Decision
For delivery failures (e.g., an invalid address), the following steps will be implemented:

1. **Failure Detection:**  
   When the Delivery Service detects a delivery failure (due to an invalid address), it marks the delivery attempt as failed.
  
2. DeliveryServiceImpl.java
```java
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
                publishDeliveryFailedStatusUpdate(command.getOrderId());

                command.setStatus(DELIVERY_FAILED.name());
                deliveryRepository.save(command);
            }
        }
   }
```
2. **Publishing the Failure Event:**  
   The Delivery Service publishes a `DELIVERY_FAILED` event to a Kafka topic. The event includes:
    - `orderId`
    - `status` set to `DELIVERY_FAILED`
    - Optionally, a `reason` indicating the failure (e.g., "INVALID_ADDRESS")  
     *(This field is not currently implemented but can be added in the future to extend the event's information.)*
    - Optionally, a `deliveryId` for tracking purposes  
      *(Also available as a future extension if more detailed tracking is required.)*

DeliveryServiceImpl.java
```java
   private void publishStatusUpdate(String orderId, DeliveryStatus status) {
   if (orderId == null || status == null) {
   log.error("Cannot publish status update with null orderId or status");
   return;
   }
   OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, status.name());
   eventPublisher.publishOrderStatusUpdate(event);
   }
```
3. **Compensating Actions by the Warehouse Service:**  
   The Warehouse Service consumes the `DELIVERY_FAILED` event. In response, it compensates by restoring the inventory for the failed order and then publishes a follow-up event indicating that the order has been cancelled due to the failed delivery:

OrdersStatusUpdateConsumer.java
```java
@KafkaListener(
topics = "${kafka.ordersStatusUpdate.topic}",
groupId = "${kafka.ordersStatusUpdate.group-id}",
containerFactory = "objectsKafkaListenerContainerFactory"
)
public void handleOrderStatusUpdateEvent(OrderStatusUpdateEventDto event) {
if (event == null) {
log.warn("Received null OrderStatusUpdateEvent");
return;
}

    log.info("Received OrderStatusUpdateEvent: {}", event);

    if (DELIVERY_FAILED.name().equals(event.getStatus())) {
        try {
            warehouseService.compensateInventory(event);
        } catch (Exception ex) {
            log.error("Failed to compensate inventory for order {}. Retrying...", event.getOrderId(), ex);
            throw ex;
        }
    }
}
```

4. **Order Service Update and Customer Notification:**  
   The Order Service receives the cancellation event from the Warehouse Service, updates the order status to reflect the failure, and notifies the customer that the delivery was unsuccessful—prompting them to update their address if needed.

This process ensures that when a delivery error occurs, all affected services perform their respective compensating actions asynchronously, maintaining system consistency and enabling future enhancements with more sophisticated resilience patterns.

## Consequences

### Pros
- **Resilience and Recovery:**  
  The system can recover gracefully from delivery failures by rolling back inventory allocations and promptly notifying customers.
- **Loose Coupling:**  
  Using asynchronous messaging via Kafka decouples the Delivery, Warehouse, and Order services, improving scalability.
- **Future Extensibility:**  
  The design sets a solid foundation for integrating advanced patterns like stateful retries or orchestrated sagas with Camunda BPM.

### Cons
- **Increased Complexity:**  
  Managing asynchronous event flows and ensuring eventual consistency across services adds complexity.
- **Monitoring Challenges:**  
  Tracking the flow of failure events across multiple services requires robust monitoring and logging mechanisms.

## Alternatives Considered
- **Synchronous Error Handling:**  
  Rejected due to tight coupling and potential performance bottlenecks.


## Conclusion

  By adopting this event-driven error handling approach, our system will be able to handle delivery failures effectively. This ensures that inventory is correctly compensated, order statuses are updated, and customers are informed about delivery issues—all while maintaining a loosely coupled and scalable architecture. Future extensions can incorporate additional fields (like a detailed `reason` or `deliveryId`) as needed.

