# Architecture Decision Record (ADR)

## 1. Title
**Enable Multi-Item Orders and Event-Driven Publication in Order Service**  
GitHub Issue: **#13**

## 2. Status
**Accepted**

## 3. Context
The existing Order Service required the ability to handle multiple items per order (instead of a single specification) and emit domain events. We needed to accommodate new fields (list of items, item IDs, quantities, etc.), publish an `OrderCreatedEvent`, and maintain alignment with the extended PlantUML diagram.

## 4. Decision
- **Order Model** was updated to store a list of items (`OrderItem`) rather than a single `OrderSpecification`.
- **OrderCreatedEvent** was introduced (or updated) to reflect these new fields.
- **OrderController** and **OrderProducer** were changed to build and publish `OrderCreatedEvent` messages to Kafka under the `order-events` topic.

## 5. Justification
1. **Scalability**: Allows orders to contain any number of items with arbitrary SKUs or IDs.
2. **Decoupling**: Publishing `OrderCreatedEvent` via Kafka ensures other services can react asynchronously.
3. **Consistency with Diagram**: Aligns code with the extended PlantUML model, reflecting multi-item orders and event-driven workflows.

## 6. Consequences
- **Positive**
    - Clear contract for multi-item orders.
    - Facilitates asynchronous processing (payment, inventory checks, etc.).
- **Negative**
    - Requires consumers of `OrderCreatedEvent` to adapt to the new schema (list of items).
    - Additional complexity in serialization/deserialization of the new event structure.

## 7. Implementation Details
- **Order** now includes a `List<OrderItem>` and corresponding fields (`status`, `createdAt`).
- **OrderItem** represents each item’s `itemId` and `quantity`.
- **OrderCreatedEvent** references this list of items, along with `orderId`, `location`, `orderDate`, and `timestamp`.
- **OrderController** publishes `OrderCreatedEvent` to Kafka on `POST /orders`.
- **OrderProducer** can still generate sample events via a scheduled job for demonstration or testing.

## 8. Alternatives Considered
- **Single SKU per order**: Insufficient for new requirements, inflexible for real-world multi-item orders.
- **Synchronous communication**: Less suitable for decoupled microservices; event-driven design was preferred.

**Outcome**: The revised multi-item order model and event-driven updates were implemented and accepted.