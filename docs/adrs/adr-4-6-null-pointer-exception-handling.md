# ADR-57: Robust Null Handling in Microservices Communication

## Status

Accepted

## Context

Our microservices architecture consists of three main services: Order Service, Warehouse Service, and Delivery Service, which communicate with each other through Kafka events and REST API calls. During testing, we encountered NullPointerExceptions in the Warehouse Service when processing orders with null or missing fields. This caused service failures and disrupted the order fulfillment process.

The specific issue was in the `InMemoryInventoryRepository.findByItemId()` method, which was attempting to use a null key in a ConcurrentHashMap, resulting in the error:
```
java.lang.NullPointerException: Cannot invoke "Object.hashCode()" because "key" is null
```

Additionally, we found that the Delivery Service was failing to start due to Docker Compose integration issues, and there was a port conflict with the Warehouse Service.

## Decision

We decided to implement comprehensive null checking throughout the codebase to make our services more robust against unexpected data. The changes included:

1. **Add null checks in repository methods:**
   - Added null checks in `InMemoryInventoryRepository.findByItemId()` and `save()` methods
   - Return empty Optional when null IDs are provided

2. **Add null checks in service methods:**
   - Added null validation in `InventoryServiceImpl` methods
   - Added null validation in `WarehouseServiceImpl` methods
   - Added null validation in `DeliveryServiceImpl` methods
   - Added proper error handling in `DeliveryCommandServiceImpl`

3. **Fix configuration issues:**
   - Disabled Docker Compose integration in Delivery Service
   - Updated port configuration to avoid conflicts (Warehouse Service now uses port 8084)
   - Updated service URLs to reflect the new port configuration

4. **Improve error handling:**
   - Added try-catch blocks around critical operations
   - Added proper logging for error scenarios
   - Ensured services can continue operating even when receiving malformed data

## Consequences

### Positive

- Services are now more resilient to null or malformed data
- Error messages are more descriptive, making debugging easier
- Services can continue operating even when receiving unexpected data
- The system can handle partial failures without crashing completely
- Improved logging provides better visibility into error scenarios

### Negative

- Increased code complexity due to additional null checks
- Slightly increased processing overhead due to validation logic
- Some error scenarios are now silently logged rather than failing fast, which could potentially mask issues

## Test Case

We tested the solution with the following order request:

```json
{
    "orderId": "6722330",
    "requestedItems": [
        {
            "sku": "ITEM-001",
            "quantity": 2
        },
        {
            "sku": "ITEM-002",
            "quantity": 5
        }
    ]
}
```

The system now successfully processes this order without throwing NullPointerExceptions, even when some fields might be missing or null in the processing chain.