# ADR: Error Handling in Order Service

**Status:** Accepted  
**Date:** 2025-03-24

## Context

The Order Service processes customer orders and must handle various error scenarios, including:
- Invalid input data (e.g., missing fields, invalid formats)
- Domain-specific validation failures (e.g., invalid delivery locations)
- Business rule violations (e.g., negative item quantities)
- System errors (e.g., database failures, communication issues)

Proper error handling is crucial for:
- Providing clear feedback to users
- Maintaining data integrity
- Ensuring system reliability
- Supporting troubleshooting and debugging

## Decision

We will implement a comprehensive error handling strategy in the Order Service with the following components:

1. **Domain-Specific Exceptions**:
   - `InvalidDeliveryLocationException` - For unsupported delivery locations
   - `InvalidOrderSpecificationException` - For invalid order details

2. **Validation Service**:
   - Centralized validation logic in `OrderValidationService`
   - Validation of delivery locations (restricted to Swiss cities)
   - Validation of order items (positive quantities required)

3. **Global Exception Handler**:
   - Spring `@ControllerAdvice` to handle exceptions across all controllers
   - Consistent error response format
   - Appropriate HTTP status codes for different error types

4. **Structured Error Responses**:
   - JSON format with timestamp, status code, error type, and message
   - Clear, user-friendly error messages
   - Sufficient detail for client applications to handle errors appropriately

### Error Response Format

```json
{
  "timestamp": "2025-03-24T21:34:56.789Z",
  "status": 400,
  "error": "Invalid Delivery Location",
  "message": "Delivery location 'Munich' is not supported. We currently only deliver to major Swiss cities."
}
```

## Consequences

### Positive

- **Improved User Experience**: Clear error messages help users understand and fix issues
- **Data Integrity**: Validation prevents invalid data from entering the system
- **Maintainability**: Centralized error handling simplifies code and reduces duplication
- **Troubleshooting**: Structured error responses facilitate debugging and support

### Negative

- **Development Overhead**: Implementing comprehensive validation requires additional code
- **Maintenance Burden**: Error handling logic must be kept in sync with business rules
- **Performance Impact**: Validation checks add processing overhead

## Implementation Details

1. **Exception Classes**:
   - Custom exception classes extending `RuntimeException`
   - Meaningful error messages that explain the issue

2. **Validation Service**:
   - Validation methods for different aspects of an order
   - Configurable validation rules (e.g., list of supported delivery locations)
   - Clear separation of validation concerns

3. **Controller Integration**:
   - Validation called before processing commands
   - Exceptions propagated to the global exception handler

4. **Global Exception Handler**:
   - Different handling for different exception types
   - Consistent error response structure
   - Appropriate HTTP status codes

+ ### Invalid Examples and Error Responses

#### Example 1: Invalid Delivery Location

**Request:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "orderDate": "2025-03-24",
  "deliveryLocation": "Munich",
  "requestedItems": [
    {
      "itemId": "ITEM-001",
      "quantity": 2
    }
  ]
}' http://localhost:8091/orders
```

**Response:**
```json
{
  "timestamp": "2025-03-24T21:34:56.789Z",
  "status": 400,
  "error": "Invalid Delivery Location",
  "message": "Delivery location 'Munich' is not supported. We currently only deliver to major Swiss cities."
}
```

#### Example 2: Invalid Item Quantity

**Request:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "orderDate": "2025-03-24",
  "deliveryLocation": "Zurich",
  "requestedItems": [
    {
      "itemId": "ITEM-001",
      "quantity": -1
    }
  ]
}' http://localhost:8091/orders
```

**Response:**
```json
{
  "timestamp": "2025-03-24T21:35:12.456Z",
  "status": 400,
  "error": "Invalid Order Specification",
  "message": "Invalid quantity for item ITEM-001: -1"
}
```

#### Example 3: Missing Required Fields

**Request:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "orderDate": "2025-03-24",
  "deliveryLocation": "Zurich"
}' http://localhost:8091/orders
```

**Response:**
```json
{
  "timestamp": "2025-03-24T21:36:05.123Z",
  "status": 400,
  "error": "Invalid Order Specification",
  "message": "Order must contain at least one item"
}
```

## Alternatives Considered

1. **Bean Validation (JSR 380)**:
   - Would provide annotation-based validation
   - Rejected because our domain validation is more complex than what annotations can express

2. **Service-Level Error Codes**:
   - Would return error codes instead of exceptions
   - Rejected due to reduced clarity and increased coupling

3. **Per-Controller Exception Handling**:
   - Would handle exceptions in each controller separately
   - Rejected due to code duplication and inconsistency risks

## Conclusion

The implemented error handling strategy provides a robust foundation for the Order Service. By combining domain-specific exceptions, centralized validation, and a global exception handler, we achieve clear error communication, data integrity, and system reliability. The approach balances development effort with the benefits of comprehensive error handling.