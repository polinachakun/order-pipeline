# ADR: Commands vs Events in the  Project

**Status:** Accepted  
**Date:** 2025-03-20

## Context

In our distributed order management system, several services participate in fulfilling an order lifecycle:

- **Order Service**
- **Warehouse Service**
- **Factory Service**
- **Delivery Service**
- **Apache Kafka** as the event bus

We aim to balance **event-driven choreography** with **command-based orchestration**.

While events provide loose coupling and flexibility, some interactions require **strong coordination** or **acknowledgment**, which favors command-based communication.

## Decision

We adopt a **hybrid approach**:

- Use **commands** for interactions that require immediate processing or acknowledgment.
- Use **events** for status broadcasting, asynchronous updates, or when multiple subscribers might react.

### Interaction Mapping

| Interaction                             | Type       | Justification                                                                 |
|----------------------------------------|------------|-------------------------------------------------------------------------------|
| Order → Warehouse                      | Command    | The order service must ensure the warehouse has accepted and will process the order. |
| Warehouse → Delivery                   | Command    | Warehouse is directly responsible for initiating delivery and expects confirmation. |
| Warehouse → Factory                    | Event / Command | Currently an event. Can be changed to command if production coordination is required. |
| Warehouse → Order (status updates)     | Event      | Fulfillment statuses  are broadcast asynchronously. |
| Delivery → Order (status updates)      | Event      | Delivery status changes  are events; no confirmation needed. |
| Factory → Warehouse (stock arrival)    | Event      | Inventory changes are published via events for async updates across services. |

## Changes from Initial Design

| Interaction              | From  | To     | Reason                                                   |
|--------------------------|-------|--------|----------------------------------------------------------|
| Order → Warehouse        | Event | Command | Requires acknowledgment and business validation upfront. |
| Warehouse → Delivery     | Event | Command | Warehouse orchestrates delivery explicitly.              |

Command from Warehouse to Delivery
Warehouse explicitly triggers delivery via REST call using a command object.

WarehouseServiceImpl.java
```java
StartDeliveryCommand payload = new StartDeliveryCommand(
orderDto.getOrderId(),
orderDto.getDeliveryLocation()
);

log.info("Sending delivery command for order {}", orderDto.getOrderId());
deliveryCommand.execute(payload);
```
DeliveryCommandServiceImpl.java
```java
@Override
public void execute(AbstractDto payload) {
if (payload == null) {
log.error("Cannot execute command with null payload");
return;
}

    try {
        StartDeliveryCommand commandPayload = (StartDeliveryCommand) payload;

        log.info("Sending start delivery command to {}: {}", deliveryServiceUrl, commandPayload);
        restTemplate.postForLocation(deliveryServiceUrl, commandPayload);
        log.info("Successfully sent delivery command");
    } catch (Exception e) {
        log.error("Failed to send delivery command: {}", e.getMessage(), e);
    }
}
```
DeliveryServiceController.java
```java
@PostMapping()
public ResponseEntity<String> startDelivery(@RequestBody StartDeliveryCommand command) {
if (command == null) {
log.error("Received null delivery command");
return ResponseEntity.badRequest().body("Command cannot be null");
}

    log.info("Received start delivery command: {}", command);

    String deliveryId = deliveryService.handleStartDeliveryCommand(command);

    if (deliveryId == null) {
        return ResponseEntity.badRequest().body("Failed to start delivery");
    }

    return ResponseEntity.ok(deliveryId);
}
```

Although the `Order → Warehouse` interaction currently uses an event-based mechanism, the `Warehouse Service` is already designed to support command-based integration.

The relevant handler class and endpoint exist to accept a command payload when the interaction is transitioned to a command-style trigger. This ensures minimal refactoring is required when the business logic demands stricter coordination or acknowledgment.

The handler is ready to process a command containing full order details:
WarehouseCommandController.java
```java
    @PostMapping("/process")
    public ResponseEntity<String> processOrder(@RequestBody OrderDto orderDto) {

        log.info("Received process order command for order: {}", orderDto.getOrderId());
        warehouseService.processNewOrder(orderDto);

        return ResponseEntity.ok(orderDto.getOrderId());
    }
```
## Consequences

- Improved coordination for mission-critical flows
- Status and inventory updates remain fully decoupled
- Enables flexibility and extensibility through event-driven design
- Retains service autonomy and reactivity where appropriate

## Alternatives Considered

- **Fully event-based choreography**  
More decoupled, but harder to trace, coordinate, and recover from failure

- **Fully centralized orchestration**  
Simpler to manage, but less scalable and increases coupling

## Conclusion

This hybrid approach combines the **flexibility of events** with the **control of commands**. It ensures robust communication and coordination in the most business-critical flows while keeping the overall system reactive and scalable.
