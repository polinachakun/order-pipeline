# ADR: Commands vs Events in Order Service

**Status:** Accepted  
**Date:** 2025-03-24

## Context

The Order Service is a central component in our event-driven architecture, responsible for:
- Creating new orders
- Tracking order status
- Communicating with other services (Warehouse, Factory, Delivery)

We need to determine the appropriate communication patterns for the Order Service, balancing:
- Synchronous vs. asynchronous communication
- Direct commands vs. event-based notifications
- User-facing API design vs. inter-service communication

## Decision

We will implement a hybrid approach in the Order Service:

1. **REST API (Command-based)** for user interactions:
   - `POST /orders` - Command to create a new order
   - `GET /orders/{orderId}` - Query to retrieve order status
   - `PUT /orders/{orderId}/status` - Command to update order status

2. **Kafka Events** for inter-service communication:
   - Publish `OrderCreatedEvent` when a new order is created
   - Consume `OrderStatusUpdateEvent` from other services
   - Maintain eventual consistency through event-based updates

### Communication Pattern Mapping

| Interaction | Pattern | Implementation | Justification |
|-------------|---------|----------------|---------------|
| User → Order Service (create) | Command | REST POST | Immediate feedback needed for user experience |
| User → Order Service (query) | Query | REST GET | Synchronous response required for UI |
| Order Service → Other Services | Event | Kafka message | Asynchronous, decoupled communication |
| Other Services → Order Service | Event | Kafka message | Status updates don't require immediate processing |

## Consequences

### Positive

- **Improved User Experience**: Synchronous REST API provides immediate feedback
- **Decoupled Services**: Event-based communication maintains service autonomy
- **Scalability**: Asynchronous processing allows for better load handling
- **Flexibility**: Services can evolve independently with minimal coordination

### Negative

- **Complexity**: Managing both synchronous and asynchronous patterns increases complexity
- **Eventual Consistency**: Status updates may have delays due to event processing
- **Error Handling**: More complex error handling across different communication patterns

## Implementation Details

1. **REST Controller**:
   - Implements command and query endpoints
   - Validates input data before processing
   - Returns appropriate HTTP status codes and responses

2. **Event Publishers**:
   - Publish events to Kafka topics after successful command processing
   - Include all relevant order data in the events

3. **Event Consumers**:
   - Listen to relevant Kafka topics for status updates
   - Update the local order repository based on received events

## Alternatives Considered

1. **Fully Synchronous API**:
   - Would simplify implementation but reduce scalability
   - Rejected due to tight coupling between services

2. **Fully Event-Driven**:
   - Would maximize decoupling but complicate user interactions
   - Rejected due to poor user experience for immediate feedback

## Conclusion

The hybrid approach balances user experience needs with system scalability requirements. By using commands for user interactions and events for inter-service communication, we achieve a responsive user interface while maintaining a loosely coupled, scalable backend architecture.