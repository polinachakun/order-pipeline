# ADR: Orchestration vs Choreography in the Project

**Status:** Accepted  
**Date:** 2025-03-20

## Context

The EDPO project is a distributed system composed of several microservices:

- **Order Service**
- **Warehouse Service**
- **Factory Service**
- **Delivery Service**
- Kafka is used as the asynchronous event bus
- Camunda (BPMN process engine) for process orchestration

The services interact to process customer orders, check stock, request missing items from the factory, manage delivery, and update the order lifecycle through events or commands.

We must decide whether the system coordination should rely primarily on **choreography** (peer-to-peer communication via events) or **orchestration** (centralized control through a coordinator).

## Decision

We adopt a **hybrid approach**, combining **event-driven choreography** and **explicit orchestration**, depending on the role and criticality of the interaction.

### Event-Driven Choreography
- Most status updates and inventory-related flows are handled via **asynchronous Kafka events**
- Services are decoupled and publish/consume events independently
- Ideal for flows like:
    - Order status updates
    - Fulfillment and packing progress
    - Factory stock replenishment

### Command-Driven Orchestration 
- Used for **tight control over critical steps**
- For example:
    - `WarehouseService → DeliveryService`: delivery is started via a command with payload and acknowledgment
    - `OrderService → WarehouseService`: (planned) creating an order may be coordinated via a command to ensure acceptance

### Camunda Integration
- BPMN-based processes (via Camunda) are  to define and manage complex business workflows
- Camunda acts as the **central orchestrator** for multi-step scenarios, e.g.:
    - Partial fulfillment + wait for stock + retry delivery
    - Timeout-based escalation for delivery
- Camunda delegates or external tasks will be used to interact with microservices asynchronously or via commands


## Consequences

- **Choreography** supports **loose coupling**, scalability, and independent service evolution
- **Orchestration** provides **strong coordination** and **process visibility** for critical actions
- Hybrid approach balances both worlds:
  - Reactive event-driven architecture
  - Controlled process execution where needed


- We gain flexibility in building robust, decoupled systems
- Events are easy to extend for future consumers (e.g. notification, analytics)
- Commands ensure accountability for irreversible steps like delivery
- Camunda allows visual modeling and orchestration of more complex workflows

## Alternatives Considered

| Approach          | Pros                                     | Cons                                                   |
|------------------|------------------------------------------|--------------------------------------------------------|
| Full choreography | Decoupled, scalable                      | Hard to track process, handle errors                   |
| Full orchestration| Central control, clear flow management   | Higher coupling, single point of failure if not careful|

## Conclusion

The hybrid coordination approach enables us to make pragmatic decisions per use case:
- Events for loosely coupled, observable, extensible flows
- Commands for business-critical, controlled operations
- Camunda for visual and flexible workflow orchestration

This decision aligns with modern microservice best practices and the complexity of our business domain.

