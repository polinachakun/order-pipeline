# **ADR: Camunda Integration with Kafka for Order Processing (GH Issue #21)**

## **1. Context**
The system consists of three services:
- **Order Service** (handles new orders)
- **Factory Service** (processes orders)
- **Warehouse Service** (manages stock and shipping)

A workflow is needed to orchestrate the interactions between these services, using Camunda BPMN and Kafka for event-driven communication.

## **2. Decision**
- **Camunda BPMN is generated dynamically using Java**, instead of manually creating BPMN files.
- **Delegate expressions** (`#{service.method}`) are used to integrate Kafka producers and service logic.
- **Parallel execution** is implemented for concurrent order processing and stock checking.
- **Decision gateways** manage conditions like stock availability.

## **3. Implementation Details**
- **BPMN Generation**
    - Implemented in `CamundaBpmnGenerator.java`
    - Generates `generated_order_process.bpmn`
    - Includes **service tasks** to trigger Kafka events
    - Uses **parallel gateways** for simultaneous tasks
    - Uses **exclusive gateways** for conditional logic

- **Deployment and Execution**
    - The BPMN file is deployed via Camunda REST API
    - The process is triggered programmatically or via Camunda Cockpit
    - Kafka is used for asynchronous communication between services

- **Other Changes**
    - Adjusted pom.xml for parent dependencies in all services
    - Added parent pom.xml for shared dependencies ie camunda

