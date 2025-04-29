# edpoa-fs2025
Event-driven Process-oriented Architectures FS 2025 at University St. Gallen

This project simulates an end-to-end distributed order processing system using Kafka and microservices. It includes services responsible for order handling, warehouse operations, inventory management, and delivery coordination.

---

## 📌 Flow Overview

The system includes the following stages:

1. **Order Creation** – User places an order via the Order Service.
2. **Warehouse Processing** – Order is picked and packed (or partially fulfilled).
3. **Factory Replenishment** – Missing items are requested from the factory.
4. **Delivery Execution** – The Delivery Service delivers the order.

---

### 1. Start Kafka and Dependencies

Use Docker Compose to start Kafka and other required services:

```bash
docker-compose up --build
```
Alternatively, if already built:
```bash
docker-compose up
```

### 2. Start Microservices
   Currently, the following services are available for local testing:

| Service           | Status         | How to Run                                                                                                                            |
|-------------------|----------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Order Service     | ✅ Ready       | Run [OrderServiceApplication.java](order-service/src/main/java/com/example/OrderServiceApplication.java)                              |
| Factory Service   | 🚧 In progress | Not available                                                                                                                         |
| Warehouse Service | ✅ Ready       | Run [WarehouseServiceApplication.java](warehouse-service/src/main/java/com/example/warehouseservice/WarehouseServiceApplication.java) |
| Delivery Service  | ✅ Ready       | Run [DeliveryServiceApplication.java](delivery-service/src/main/java/com/example/deliveryservice/DeliveryServiceApplication.java)     |


Logs: Follow order status transitions in the logs of Order, Warehouse and Delivery services.

### 3. Testing the Services

#### 3.1 Testing the Order Service

The Order Service provides a REST API for creating and managing orders. It validates orders and publishes events to Kafka.

##### Order Service Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/orders` | POST | Create a new order |
| `/orders/{orderId}` | GET | Get order status by ID |
| `/orders/{orderId}/status` | PUT | Update order status |

##### Example Requests

###### Create a Valid Order
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "orderDate": "2025-03-24",
  "deliveryLocation": "Zurich",
  "requestedItems": [
    {
      "itemId": "ITEM-001",
      "quantity": 2
    },
    {
      "itemId": "ITEM-002",
      "quantity": 3
    }
  ]
}' http://localhost:8091/orders
```

###### Create an Invalid Order (Wrong Location)
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

###### Create an Invalid Order (Negative Quantity)
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

###### Get Order Status
```bash
curl -X GET http://localhost:8091/orders/{orderId}
```
Replace `{orderId}` with the actual order ID returned from the create order request.

###### Update Order Status
```bash
curl -X PUT "http://localhost:8091/orders/{orderId}/status?status=SHIPPED"
```
Replace `{orderId}` with the actual order ID and `status` with the desired status.

##### Validation Rules

The Order Service validates:
- **Delivery Location**: Must be a valid Swiss city (e.g., Zurich, Bern, Geneva)
- **Order Items**: Each item must have a positive quantity
- **Required Fields**: Order must contain at least one item

##### End-to-End Flow with Order Service

1. **Create an order** using the Order Service REST API
2. The Order Service validates the order and publishes an `OrderCreatedEvent` to Kafka
3. The Warehouse Service consumes the event and processes the order
4. The Warehouse Service publishes status updates as the order is processed
5. The Delivery Service receives the order when it's ready for delivery

#### 3.2 Testing the Order, Warehouse and Delivery Services

You can test the Kafka-based communication between **Order**, **Warehouse** and **Delivery** services using a few mocked requests.

Use the provided [Postman Collection](https://edpoa-fs-2025-hsg.postman.co/workspace/EDPOA-FS-2025-HSG~332ac8aa-2b9a-4f3c-888e-9d10c3082868/request/42898767-14e9faa5-02d8-49c7-a85a-fe1bd1533dde?action=share&creator=42898767&ctx=documentation)
or follow the `curl` instructions below.

---

#### 🟢 To simulate the **Happy Path**:

You have to **first restock the inventory** (Steps 3.2.2 and 3.2.3), ensuring that all items are available.
Then, **create a new order** (Step 3.2.1).
The Warehouse will immediately fulfill the order and send it for delivery.

#### 🔴 To simulate the **Missing Item Flow**:
**Create a new fake order first** (Step 3.2.1) – before restocking inventory.
Then **restock the inventory** (Steps 3.2.2 and 3.2.3).

---

#### Step 3.2.1: Publish a new Order to Kafka

```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "orderDate": "2025-03-24",
  "deliveryLocation": "Zurich",
  "requestedItems": [
    {
      "itemId": "ITEM-001",
      "quantity": 2
    },
    {
      "itemId": "ITEM-002",
      "quantity": 3
    }
  ]
}' http://localhost:8091/orders
```
#### Step 3.2.2: Partially Restock Inventory (to simulate partial fulfillment)
```bash
curl --location --request GET 'http://localhost:8082/factory/items' \
--header 'Content-Type: application/json' \
--data '{
    "itemId": "ITEM-002",
    "quantity": 5
}'
```
#### Step 3.2.3: Fully Restock Inventory (to simulate Happy Path)
```bash
curl --location --request GET 'http://localhost:8082/factory/items' \
--header 'Content-Type: application/json' \
--data '{
    "itemId": "ITEM-001",
    "quantity": 2
}'
```
#### Step 3.2.4: Check Inventory Status
```bash
curl --location --request GET 'http://localhost:8082/factory/items' \
--header 'Content-Type: application/json' \
--data '{
    "itemId": "ITEM-002",
    "quantity": 5
}'
```
#### Step 3.2.5: Check Order Status
```bash
curl --location --request GET 'http://localhost:8082/orders' \
--header 'Content-Type: application/json' \
--data '{
    "orderId": "6722330",
    "items": [
        { "sku": "ITEM-001", "quantity": 2 }
    ]
}'
```
#### Step 3.2.6: Check Delivery Records
```bash
curl --location 'http://localhost:8083/delivery'
```




