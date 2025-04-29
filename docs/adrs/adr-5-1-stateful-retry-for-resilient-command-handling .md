# ADR: Implementation of Stateful Retry for Resilient Command Handling

**Status:** Accepted  
**Date:** 2025-04-10

---

## Context

In our distributed order management system, various services (e.g., Order Service, Warehouse Service, Delivery Service, etc.) exchange commands and events via an Event Bus. Commands such as `processNewOrder` or `Start delivery` initiate different stages of the business process. It is crucial that these commands not only get delivered but are also executed correctly even in the face of transient failures or temporary unavailability of some services.

To achieve high reliability, we are employing **Stateful Retry**—one of the key stateful resilience patterns. This pattern enables us to persist the state of command attempts, automatically retry execution, and orchestrate compensatory actions to correct errors if they occur.


---

## Decision

We have refactored the interaction between Warehouse Service and Delivery Service to use a Stateful Retry mechanism instead of a direct REST call.

Now, when Warehouse decides to initiate delivery (after successful item picking), it no longer calls the Delivery service synchronously.
Instead, it creates a retryable command (StartDeliveryCommand), which is persisted, tracked, and retried automatically until successfully delivered.
The system uses:

- **`StatefulRetryCommandHandler`** – to store, execute, and retry commands.
- **`RetryScheduler`** – to periodically scan and re-execute pending commands.
- **`InMemoryCommandRetryRepository`** – to track command status (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`).
- **`DeliveryCommandServiceImpl`** – to send idempotent HTTP `POST` requests to Delivery.

This change ensures that even if the Delivery Service is temporarily unavailable, the command will be retried until successful or marked as failed, maintaining **reliability and decoupling** between services.

This change ensures that even if the Delivery Service is temporarily unavailable, the command will be retried until successful or marked as failed, maintaining reliability and decoupling between services.

### Core Components:

Command State Tracking

```java
   public class CommandRetryEntity {
   enum RetryStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED }

   private String id;
   private AbstractDto payload;
   private RetryStatus status;
   private int retryCount;
   private LocalDateTime nextRetryAt;
   }
   ```
Stored in-memory using:

```java
@Component
public class InMemoryCommandRetryRepository {
private final Map<String, CommandRetryEntity> storage = new ConcurrentHashMap<>();
}
```
Retry Handler for saving command and retries on failure:

```java
public void handle(StartDeliveryCommand command) {
CommandRetryEntity retryEntity = new CommandRetryEntity(...);
retryRepository.save(retryEntity);
executeWithRetry(retryEntity);
}

public void executeWithRetry(CommandRetryEntity entity) {
try {
deliveryCommandService.execute((StartDeliveryCommand) entity.getPayload());
entity.setStatus(COMPLETED);
} catch (Exception e) {
entity.setRetryCount(entity.getRetryCount() + 1);
entity.setNextRetryAt(LocalDateTime.now().plusSeconds(10));
entity.setStatus(PENDING);
}
retryRepository.save(entity);
}
```
Scheduler to scans every 20 seconds and triggers retry:

```java
@Scheduled(fixedDelay = 20000)
public void retryPendingCommands() {
var pending = retryRepository.findByStatusAndNextRetryAtBefore(PENDING, now());
for (var cmd : pending) retryHandler.executeWithRetry(cmd);
}
```

Command Execution (idempotent HTTP call to Delivery service):

```java
public void execute(AbstractDto payload) {
restTemplate.postForLocation(deliveryServiceUrl, (StartDeliveryCommand) payload);
}
```

Integration Point in Warehouse when order is ready:

```java
statefulRetryCommandHandler.handle(new StartDeliveryCommand(orderId, location));
```
To ensure transparency and operational control, a monitoring endpoint is available for viewing the current state of all retryable commands:

```json
GET /api/monitoring/commands
```
This returns a list of tracked commands with full retry metadata.
```json
[
{
"id": "b4071949-3af0-4d80-937c-417aa72f5a4b",
"commandType": "StartDeliveryCommand",
"payload": {
"orderId": "11122300",
"deliveryLocation": "Zurich"
},
"status": "COMPLETED",
"retryCount": 1,
"nextRetryAt": "2025-04-10T18:08:48.623292",
"lastAttemptAt": "2025-04-10T18:09:03.516312"
}
]
```
---

## Alternatives Considered and Rationale for the Choice

- **Synchronous Calls (REST/RPC):**  
  While synchronous calls might offer immediate feedback, they tie the services too closely together and do not offer built-in mechanisms for compensating failures in a distributed environment.

- **Asynchronous Interaction Without State Preservation:**  
  Operating asynchronously without maintaining state increases the risk of command loss, jeopardizing the integrity of business processes and potentially requiring manual intervention.

The chosen solution effectively balances the benefits of asynchronous communication (i.e., low coupling and scalability) with the necessary guarantees for reliable command delivery and processing through automated retries.

---

## Consequences

### Pros
- **Enhanced Resilience:**  
  The automatic retry mechanism guarantees that commands will eventually be delivered and processed even amidst temporary disruptions.
- **Reduced Operational Failures:**  
  Thanks to idempotence and state preservation, the system can gracefully handle duplicate commands, minimizing errors and manual recovery.
- **Scalability:**  
  Asynchronous processing enables independent service scaling, contributing to an overall more responsive and flexible system design.

### Cons
- **Increased Implementation Complexity:**  
  Developing mechanisms for state storage and managing retry logic adds complexity to the system.
- **Complex Monitoring and Debugging:**  
  The asynchronous nature and retry processes complicate tracking and debugging, necessitating robust logging and alerting systems.

---

## Conclusion

Using **Stateful Retry** as the chosen stateful resilience pattern ensures reliable command processing in our distributed architecture.
Thus, we rely on the Stateful Retry pattern to automatically handle errors and ensure stable command execution, enabling our system to remain robust even in the face of repeated transient failures.
