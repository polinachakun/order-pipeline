# ADR-002: Outbox Pattern and Transactional Boundaries 

## Status

**Status:** Proposed  
**Date:** 2025-04-12

## Context

Our distributed order management system consists of multiple microservices (Order, Warehouse, Factory, Delivery) that need to communicate reliably via asynchronous messaging. The implementation of business processes like the Address Change Saga requires guaranteed message delivery between services to maintain data consistency and ensure proper process execution.

Currently, our services face several challenges:

1. **Dual-write problem**: Services need to update their internal state and send messages to other services as a single atomic operation
2. **Message delivery failures**: Temporary network issues or service unavailability can lead to lost messages
3. **Unclear transactional boundaries**: Services lack clearly defined transactional scopes, leading to potential inconsistencies
4. **Duplicate message handling**: Services may receive the same message multiple times due to retry mechanisms

These issues can cause data inconsistencies, incomplete process execution, and ultimately affect customer experience, especially in critical flows like order processing and delivery management.

## Decision

We will implement the Outbox Pattern across all our microservices and establish clear transactional boundaries to ensure reliable messaging and data consistency. This pattern will be a foundational element supporting our saga implementations, including the Address Change Saga.

### Outbox Pattern Implementation

Each service that needs to send messages to other services will:

1. **Maintain an outbox table** in its database.

2. **Use local database transactions** to atomically update the service's domain data (e.g., change order status) and insert a message into the outbox table.

3. **Implement a message relay** that:
   - Periodically polls the outbox table for unprocessed messages
   - Publishes these messages to Kafka
   - Updates the messages as processed after successful publishing
   - Implements retry logic for failed publishing attempts

### Transactional Boundaries

We will establish clear transactional boundaries for each service:

1. **Order Service**:
   - Transactional scope includes order data updates and outbox entries
   - Examples: Order status changes, order cancellations, new order creation

2. **Warehouse Service**:
   - Transactional scope includes inventory updates and outbox entries
   - Examples: Inventory deduction, stock replenishment, item reservation

3. **Delivery Service**:
   - Transactional scope includes delivery status updates and outbox entries
   - Examples: Delivery creation, delivery status changes, delivery failures

4. **Factory Service**:
   - Transactional scope includes production data and outbox entries
   - Examples: Production orders, manufacturing completions

### Message Handling

For services receiving messages:

1. **Implement idempotent consumers** that:
   - Track processed message IDs
   - Check if a message has already been processed before handling
   - Safely re-process messages with the same result if received multiple times

2. **Implement message deduplication** using:
   - Message IDs included in all messages
   - A processed-messages table or cache
   - Time-based expiration of tracked message IDs


## Consequences

### Pros

- **Reliable messaging**: Guaranteed delivery of messages even during temporary service outages
- **Data consistency**: Atomic updates of service state and message publishing
- **Failure resilience**: System can recover from temporary failures automatically
- **Clear ownership**: Each service is responsible for its own data and messages
- **Scalability**: Services can scale independently while maintaining message reliability
- **Auditability**: Outbox tables provide a clear audit trail of all inter-service communication

### Cons

- **Increased complexity**: Additional components (message relay, outbox tables) to manage
- **Potential message duplication**: Systems must handle duplicate messages (addressed by idempotency)
- **Additional latency**: Asynchronous nature introduces some delay in end-to-end processes
- **Development overhead**: Additional code and testing required for outbox management

## Related Decisions

- [ADR-001: Address Change Saga Implementation](./adr-001-address-change-saga.md)

