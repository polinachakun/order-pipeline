# ADR: Event Notification

**Status:** Accepted  
**Date:** 2025-03-01

## Context

## Context

In an event-driven system, services often need to stay informed about changes occurring in other parts of the system — such as a status update, process completion, or failure. In many cases, a full domain event with all associated data is not necessary. Instead, a **lightweight event notification** is sufficient to let subscribers react appropriately.

These notifications are not intended to carry the full state of a domain entity. Rather, they act as **signals** that something has changed, enabling consumers to update local projections, trigger workflows, or simply record activity. They are particularly effective when combined with local state or prior projections built from other sources.

Event notifications provide minimal context, designed for speed, clarity, and scalability when full data is either already known or not required.

## Decision

We will continue to publish **event notifications** to inform other services of important state transitions. These events:

- **Almost always include** at minimum: `orderId` and  `status`
## Standard Status Values

The following statuses are currently defined and used across services:

| Status                        | Description                                               |
|------------------------------|-----------------------------------------------------------|
| `READY_FOR_PICKING`          | Order was accepted and is ready to be processed           |
| `PARTIALLY_FULFILLED`        | Some items are missing in warehouse stock                 |
| `FULFILLMENT_SUCCESS`        | All items were found and picked                           |
| `READY_FOR_PACKING`          | Order is prepared for packaging                           |
| `PACKED`                     | Order is packaged                                         |
| `READY_FOR_DELIVERY`         | Package is ready to be handed off to Delivery Service     |
| `SENT_TO_DELIVERY`           | Order sent to delivery                                    |
| `DELIVERY_STARTED`           | Delivery has started                                      |
| `DELIVERED`                  | Order was successfully delivered                          |
| `DELIVERY_FAILED`            | Delivery failed (e.g. invalid address)                    |
| `DELIVERY_FAILED_ORDER_CANCELLED` | Warehouse compensated inventory; order cancelled        |

- Are **published to Kafka topics** named by domain intent (e.g., `ordersStatusUpdateTopic`)

WarehouseEventPublisher.java
```java
    public void publishOrderStatusUpdate(OrderStatusUpdateEventDto event) {
        log.info("Publishing order status update to {}, {} -> {}",
                kafkaTopicConfig.getOrdersStatusUpdateName(), event.getOrderId(), event.getStatus());

        objectKafkaTemplate.send(kafkaTopicConfig.getOrdersStatusUpdateName(), event);
    }

    public void publishItemRequest(ItemDto itemDto) {
        log.info("Publishing item request to {}, itemId={}, Quantity={}",
                kafkaTopicConfig.getItemRequestTopicName(), itemDto.getItemId(), itemDto.getQuantity());

        objectKafkaTemplate.send(kafkaTopicConfig.getItemRequestTopicName(), itemDto);
    }
```
- Can be subscribed and consumed to by multiple consumers for tracking, logging, or triggering workflows:

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

Event notifications are appropriate when:
- The purpose is to signal a lifecycle change
- The event is not required to be fully self-sufficient (unlike  Event-Carried State Transfer)

This pattern covers cases where only a minimal signal is needed.

## Consequences
### Pros
-  Enables lightweight communication and event chaining 
- Keeps services loosely coupled

### Cons
- Requires discipline to avoid over-relying on notifications when full state is needed 
- Consumers must fail gracefully if referenced data is missing

## Conclusion
Event notifications provide a simple and effective way to coordinate service behavior through status signals. They work best when used together with ECST to balance performance, clarity, and autonomy in an event-driven system.