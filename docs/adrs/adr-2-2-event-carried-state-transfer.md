# ADR: Event-Carried State Transfer

**Status:** Accepted  
**Date:** 2025-03-01

## Context

Our distributed system is built on an **event-driven architecture** where services such as **Order**, **Warehouse**, **Factory**, and **Delivery** interact via **Kafka**. Communication happens **primarily through events**, though in some scenarios **commands** are used where a clear intention and immediate action is required (e.g., triggering a delivery).

To reduce coupling and improve resilience, we adopt **Event-Carried State Transfer** as a standard communication pattern across services.

Instead of sending minimal events  and requiring recipients to query for full context, we include **domain state** directly in the messages. This enables services to act independently.  
Whenever additional context is required — such as **order item details**, **delivery location**, or a **factory item request** — it is explicitly included in the event payload to allow full processing without relying on external lookups.

## Decision

All events and commands exchanged between services will **always include**:

- `orderId`: the unique identifier of the order
- `status`: a string representing the current state in the order lifecycle

However, in many cases this minimum is not sufficient. Whenever the receiving service needs additional domain context to process a message without external lookups, the full state is included in the payload.

For example:
- When the Order Service creates a new order, it sends the Warehouse Service a message that includes the full list of requested items and delivery location:
  OrderProducer.java
```java
  public void sendOrder(Order order) {
  OrderCreatedEvent event = new OrderCreatedEvent(
  order.getOrderId(),
  order.getLocation(),
  order.getOrderDate(),
  order.getItems(),
  "CREATED",
  Instant.now()
  );
  kafkaTemplate.send(new ProducerRecord<>(ordersTopic, event.getOrderId(), event));
  System.out.println("[" + applicationName + "] Sent order to Kafka: " + event.getOrderId());
  }
  ```
- When the `Warehouse Service` notifies the `Factory Service` about a missing item, it sends the full `ItemDto` object:
 WarehouseServiceImpl.java
```java
private void publishItemRequestToFactory(ItemDto itemDto) {
    if (itemDto == null || itemDto.getItemId() == null) {
        log.warn("Cannot publish item request for null item or item with null ID");
        return;
    }
    log.info("Publishing item request to factory: itemId={}, Quantity={}", itemDto.getItemId(), itemDto.getQuantity());
    eventPublisher.publishItemRequest(itemDto);
}
```
WarehouseServiceImpl.java
- When the Warehouse Service sends a command to Delivery Service, it includes structured data such as delivery location and order reference:

```java
StartDeliveryCommand payload = new StartDeliveryCommand(
    orderDto.getOrderId(),
    orderDto.getDeliveryLocation()
);

log.info("Sending delivery command for order {}", orderDto.getOrderId());
deliveryCommand.execute(payload);
}
```



## Consequences
### Pros
- Enables fully **asynchronous communication**
- Eliminates need for **chained REST calls**
- Simplifies **retry logic** and **recovery**
-  **Loose coupling** and  **autonomy** between services
### Cons
-  Slightly **larger payloads**, but acceptable for clarity and decoupling
- Requires strong discipline in **status naming** and consistency

## Conclusion

Using Event-Carried State Transfer allows each service to make informed decisions based on the message alone. By always including orderId and status, and adding other relevant domain data when necessary, we ensure the system remains robust, scalable, and easy to reason about.