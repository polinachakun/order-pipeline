# Architecture Decision Record (ADR)

## 1. Title: Event-Driven Factory Process with Kafka

### Status
**Accepted**

### Context
The factory component of the system is responsible for processing orders, managing production workflows, and integrating with various subsystems such as material retrieval and machine operations. The system needs to handle asynchronous communication efficiently and support event-driven processing to ensure real-time updates and scalability.

### Decision
We have chosen **Apache Kafka** as the primary event-streaming platform to handle messaging between different components of the factory system. The system follows a **microservices** architecture where the **Factory Service** listens for order events, triggers production workflows, and communicates progress updates via Kafka topics.

### Justification
1. **Decoupled Communication**: Kafka enables **loose coupling** between services, allowing them to operate independently while exchanging events asynchronously.
2. **Scalability**: The system needs to process large volumes of production requests efficiently, and Kafka's high-throughput event processing supports this.
3. **Fault Tolerance**: Kafka provides **replication and persistence**, ensuring messages are not lost in case of failures.
4. **Real-time Event Processing**: The factory production process involves multiple steps where event-driven updates allow near real-time tracking and orchestration of tasks.
5. **Integration with External Systems**: The factory service integrates with MQTT for machine communication, and Kafka serves as a bridge between the factory and machine operations.

---

## 2. Architectural Details

### Services in Scope
- **Order Service**: Publishes order placement events.
- **Factory Service**: Listens for order placement events, triggers production workflows, and publishes machine operation events.
- **HBW Service** (High Bay Warehouse): Manages raw material retrieval and signals when materials are ready.
- **Machine Control Services**: Consumes factory events and manages specific machine operations.

### Kafka Topics Used
| **Topic Name**          | **Purpose**                                      |
|-------------------------|--------------------------------------------------|
| `factory.order.placed`  | Event triggered when a new factory order is created |
| `factory.hbw.start`     | Signals the start of material retrieval         |
| `factory.hbw.end`       | Indicates materials are retrieved and ready     |
| `factory.events`        | General factory process events                  |

### Kafka Producers & Consumers
#### **Producers**
- **Order Service** → Produces `factory.order.placed` event.
- **Factory Service** → Produces `factory.hbw.start` and `factory.hbw.end` events.
- **HBW Service** → Produces `factory.hbw.end` event when materials are ready.

#### **Consumers**
- **Factory Service** → Consumes `factory.order.placed` to start production.
- **HBW Service** → Consumes `factory.order.placed` and processes material retrieval.
- **Machine Services** → Consumes `factory.hbw.start` and `factory.hbw.end` to execute machine operations.

---

## 3. Implementation Details

### **Factory Service Workflow**
1. **Order Received**: The `OrderService` receives an order request via REST API and publishes a `factory.order.placed` event to Kafka.
2. **Production Start**: The `FactoryService` listens to `factory.order.placed` and initiates material retrieval by publishing a `factory.hbw.start` event.
3. **Material Retrieval**: The `HBWService` consumes `factory.hbw.start`, fetches materials, and after completion, publishes `factory.hbw.end`.
4. **Processing Steps**:
    - Once materials are ready, the factory moves through **multiple processing stages**, publishing machine events as needed.
    - Each stage (e.g., heating, assembly, packaging) can emit status updates via Kafka topics.
5. **Completion & Notification**: Upon finishing production, the system publishes a `factory.events` update, signaling completion.

---

## 4. Alternatives Considered

| **Alternative** | **Reason for Rejection** |
|----------------|-------------------------|
| **Direct REST communication** | Would introduce tight coupling between services, making scalability and fault tolerance difficult. |
| **Message Queues (e.g., RabbitMQ)** | Lacks the event streaming capability required for real-time processing. |
| **Database polling** | Inefficient for handling high-throughput event-driven workloads. |

## 5. Decision Outcome

Accepted: Kafka-based event-driven processing aligns well with the factory system's requirements.
