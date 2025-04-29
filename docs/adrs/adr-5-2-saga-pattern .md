# ADR-001: Address Change Saga Implementation

## Status

**Status:** Proposed  
**Date:** 2025-04-12

## Context

Our distributed order management system follows an event-driven microservices architecture, where multiple services (Order, Warehouse, Factory, Delivery) coordinate to fulfill customer orders. One critical workflow is the address change process after an order has been placed, particularly when delivery has failed due to an incorrect address.

Currently, the system handles delivery failures through a linear process:
1. Delivery service sends a DELIVERY_FAILED status
2. Warehouse service captures this event
3. Warehouse restores inventory (compensation action)
4. Warehouse sends DELIVERY_FAILED_ORDER_CANCELLED event to Order service
5. Order service cancels the order

This approach is functional but lacks flexibility, as it immediately cancels orders upon delivery failure without giving customers an opportunity to correct address issues. The system currently lacks a robust mechanism for handling address changes, which can lead to unnecessary order cancellations, inventory churn, and poor customer experience.

## Decision

We will implement a Saga pattern for the address change process, leveraging Camunda BPM as the orchestrator. The Address Change Saga will coordinate the compensation and recovery actions across multiple services when a customer requests an address change.

### Process Flow - New Business Approach

Instead of immediately canceling orders upon delivery failure, we will implement a more customer-centric approach:

1. Delivery service sends a DELIVERY_FAILED status
2. Order service receives the failure notification
3. Order service initiates Address Change Saga
4. Customer is notified and given the opportunity to update their address
5. System waits for customer to provide updated address (with timeout)
6. If address is updated: System restarts order process with new address
7. If timeout occurs: System initiates compensation actions including:
    - Order service requests order cancellation
    - Warehouse service restores inventory
    - Customer is notified of the cancellation

This improved flow introduces a pause in the cancellation process, giving customers time to correct address issues before compensation actions are triggered.

### Saga Participants

- **Order Service**: Manages saga coordination, customer communication, order status
- **Warehouse Service**: Handles inventory compensation, order cancellation
- **Delivery Service**: Updates delivery information, cancels delivery attempts
- **Customer**: Provides updated address information

### Compensation Actions

1. **Order Cancellation**: Transition order to cancelled state
2. **Inventory Restoration**: Return items to available inventory
3. **Customer Notification**: Inform customer about cancellation
4. **Financial Refund**: Process refund for cancelled order (if applicable)

## Technical Implementation

### Camunda BPMN Implementation

The address change saga will be implemented as a BPMN process in Camunda with the following key elements:

- User Task: "Request Address Change" (allows customer service or customer to input new address)
- Event-Based Gateway: "Wait for Response" (handles address update or timeout)
- Intermediate Catch Event: "Address Updated" (triggered when customer provides new address)
- Timer Event: "2 Weeks Timeout" (triggers cancellation flow if no response)
- Service Task: "Cancel Order" (initiates cancellation process)
- Send Task: "Notify Warehouse: Order Cancelled" (tells warehouse to restore inventory)
- Send Task: "Notify User: Order Cancelled" (informs customer of cancellation)
- Send Task: "Restart Order Process" (creates new order with updated address)

### Message Flow

1. Delivery service publishes DELIVERY_FAILED event
2. Order service receives DELIVERY_FAILED event (not DELIVERY_FAILED_ORDER_CANCELLED)
3. Order service initiates Address Change Saga instead of immediate cancellation
4. Order service creates a user task for address change
5. System waits for customer response (with 2-week timeout)
6. On success:
    - Order service creates new order with updated address
    - Original failed order is linked to new order for traceability
7. On timeout:
    - Order service sends cancellation notification to Warehouse
    - Warehouse restores inventory
    - Order service marks order as cancelled
    - Customer is notified of cancellation

This revised flow delays the inventory restoration until we're certain the customer won't provide a corrected address, improving customer satisfaction while maintaining inventory accuracy.

### Error Handling

- Retry logic for communication with each service
- Idempotent operations to prevent duplicate processing
- Transaction logging for recovery and auditing
- Timeout management to prevent stuck processes

## Consequences

### Pros

- Improved customer experience by providing opportunity to correct address issues rather than immediate cancellation
- Reduced order cancellations and improved conversion rate
- More intelligent inventory management (only restoring when truly needed)
- Clear visibility of address change process status
- Reduced manual intervention in failed deliveries
- Systematic approach to handling order modifications
- Potential for increased revenue by recovering otherwise lost orders

### Cons

- Increased complexity in process management
- Additional development effort to implement saga orchestration
- Need for careful testing of compensation flows
- Potential for longer inventory hold times when address changes are in progress
- Time-based inventory reservation requires additional monitoring

## Conclusion

Implementing the Address Change Saga pattern will transform our delivery failure handling from an immediate cancellation approach to a customer-friendly recovery process. This change directly supports our business goal of reducing unnecessary cancellations and improving customer satisfaction.

The technical implementation will require careful coordination between services, but the business benefits justify the additional complexity. By giving customers an opportunity to correct address issues, we expect to recover a significant percentage of orders that would otherwise be cancelled, directly improving our conversion rates and revenue.

We will measure the success of this implementation by tracking:
1. Percentage of delivery failures recovered through address correction
2. Reduction in customer support tickets related to delivery failures
3. Improvement in customer satisfaction metrics for delivery issues
