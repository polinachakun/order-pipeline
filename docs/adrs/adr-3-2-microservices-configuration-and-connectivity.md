# ADR-57-1: Microservices Configuration and Connectivity Issues

## Status

Accepted

## Context

Our microservices architecture relies on proper configuration for inter-service communication. During testing, we encountered several configuration-related issues that prevented the services from communicating properly:

1. **Port conflicts**: The Warehouse Service was configured to use port 8082, but this port was already in use by another process, causing the service to fail to start.

2. **Docker Compose integration issues**: The Delivery Service was configured to use Docker Compose integration, but no Docker Compose file was found in the service directory, causing startup failures with the error:
   ```
   java.lang.IllegalStateException: No Docker Compose file found in directory '/delivery-service/.'
   ```

3. **Service URL misconfigurations**: The Warehouse Service was trying to communicate with the Delivery Service using an incorrect URL, resulting in connection refused errors:
   ```
   I/O error on POST request for "http://localhost:8083/delivery": Connection refused
   ```

4. **Lack of error handling in service communication**: When one service was unavailable, the calling service would fail completely rather than gracefully handling the error.

## Decision

We implemented the following changes to address these configuration and connectivity issues:

1. **Update port configurations**:
   - Changed the Warehouse Service port from 8082 to 8084 to avoid conflicts
   - Ensured all service-to-service communication used the correct ports

2. **Disable Docker Compose integration**:
   - Added `spring.docker.compose.enabled: false` to the Delivery Service's application.yml
   - This prevents Spring Boot from looking for a Docker Compose file during startup

3. **Update service URLs**:
   - Updated the Warehouse Service configuration to use the correct URL for the Delivery Service
   - Ensured all cross-service references used the updated port configurations

4. **Improve error handling in service communication**:
   - Added try-catch blocks in the `DeliveryCommandServiceImpl` to handle connection failures
   - Added proper logging for communication errors
   - Ensured services could continue operating even when downstream services were unavailable

## Consequences

### Positive

- All services now start successfully without port conflicts
- Services can communicate with each other using the correct URLs
- The system is more resilient to temporary service unavailability
- Error messages are more descriptive, making debugging easier
- Services can continue operating in a degraded mode when other services are unavailable

### Negative

- Configuration is now more complex with different ports for each service
- Some error scenarios are now silently logged rather than failing fast
- Manual coordination is required when changing service ports to ensure all references are updated

## Test Case

We tested the solution by starting all three services and sending the following order request to the Order Service:

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

The order was successfully processed through all three services, with proper communication between them:
1. Order Service received the order and published it to Kafka
2. Warehouse Service consumed the order from Kafka and processed it
3. Warehouse Service successfully communicated with the Delivery Service via REST
4. The order status was properly updated throughout the process

Additionally, we tested error scenarios by temporarily stopping the Delivery Service, and confirmed that the Warehouse Service properly handled the connection failure without crashing.