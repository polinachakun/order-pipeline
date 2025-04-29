# Architecture Decision Record (ADR)

## 1. Title: Camunda 7 vs. Camunda 8 for Workflow Orchestration (Selected Camunda 8)

### Status
**Accepted**

### Context
Our system is designed around a microservices architecture with a strong emphasis on event-driven processing using Apache Kafka. The current setup involves processing orders, managing production workflows, and integrating with various subsystems such as material retrieval and machine operations. The orchestration of these workflows is critical to ensure efficiency, scalability, and real-time updates.

After implementing our order service with robust error handling and a hybrid approach of commands and events, we need to decide on the workflow orchestration platform to manage complex business processes, especially for error scenarios and compensating transactions.

### Decision
We have decided to adopt **Camunda 8** (Zeebe) for our workflow orchestration needs.

### Justification

#### Camunda 7 Considerations:
- **Maturity and Stability**: Camunda 7 is a mature and stable platform with extensive community support and documentation.
- **Rich Feature Set**: Provides a comprehensive set of tools including Cockpit, Tasklist, and Modeler.
- **Java Integration**: Tight integration with Spring Boot and Java ecosystem.
- **External Task Pattern**: Well-established pattern for service integration.
- **Ease of Use**: Generally easier to set up and use for developers familiar with Java and Spring Boot. The embedded engine approach simplifies deployment and integration.
- **Development Experience**: More mature tooling and extensive documentation make development and debugging more straightforward.

#### Camunda 8 (Zeebe) Advantages:
- **Cloud-Native Architecture**: Built from the ground up for cloud environments, offering horizontal scalability and high availability.
- **Event-Driven Design**: Native support for event streams and asynchronous communication, aligning perfectly with our Kafka-based architecture.
- **Microservices Integration**: Job workers model fits well with our microservices approach.
- **Performance**: Optimized for high-throughput scenarios with distributed processing.
- **Stateful Processing**: Maintains process state internally, simplifying our service implementations.
- **Future-Proof**: Represents Camunda's strategic direction, ensuring long-term support and development.
- **Learning Curve**: While initially more complex to set up, the architecture is more aligned with modern cloud-native and event-driven principles, making it more intuitive for our specific use case.

### Architectural Details

#### Integration with Existing Architecture:
- **Kafka Integration**: Camunda 8 will be integrated with our Kafka event streams, consuming events to trigger workflows and publishing events upon workflow milestones.
- **Order Process Orchestration**: The order fulfillment process will be modeled in BPMN and executed by Zeebe, coordinating between services.
- **Error Handling**: Complex error scenarios (e.g., delivery failures, inventory shortages) will be managed through BPMN error events and compensation.
- **Job Workers**: Microservices will implement job workers to handle specific tasks within the workflow.

#### Implementation Approach:
1. **Process Modeling**: Define key business processes in BPMN using Camunda Modeler.
2. **Job Worker Development**: Implement job workers in our microservices to handle process tasks.
3. **Event Bridge**: Create bidirectional integration between Kafka events and Zeebe workflows.
4. **Monitoring**: Utilize Operate for process monitoring and troubleshooting.

### Alternatives Considered

| **Alternative** | **Evaluation** |
|----------------|-------------------------|
| **Camunda 7** | While mature, feature-rich, and easier to get started with, it's less aligned with our cloud-native, event-driven architecture. The external task pattern works well but doesn't leverage the full potential of our event-driven design. For a team with strong Java/Spring experience, Camunda 7 would offer a gentler learning curve but less long-term architectural alignment. |
| **Pure Choreography** | A purely event-driven choreography would lack the explicit process visualization and monitoring capabilities we need for complex workflows, especially for error handling and compensation flows. |
| **Other BPM Solutions** | Other solutions like Flowable or jBPM lack the strong event-processing capabilities and cloud-native design of Camunda 8. |

### Decision Outcome

We will adopt Camunda 8 (Zeebe) for our workflow orchestration needs. This decision is based on:

1. **Alignment with Architecture**: Camunda 8's event-driven, cloud-native design aligns perfectly with our existing architecture.
2. **Scalability**: The platform can scale horizontally to handle our growing processing needs.
3. **Error Handling**: Provides robust capabilities for managing complex error scenarios and compensating transactions.
4. **Future-Proofing**: Represents Camunda's strategic direction, ensuring long-term viability.
5. **Developer Experience**: The BPMN modeling tools and clear separation of concerns simplify development and maintenance.

While we acknowledge that Camunda 7 might be easier to set up initially and has more mature documentation, we believe the architectural benefits of Camunda 8 outweigh the steeper learning curve. The team is prepared to invest time in learning the new platform to gain the long-term benefits of better alignment with our event-driven architecture.

### Implementation Plan

1. **Initial Setup**: Deploy Camunda 8 components (Zeebe, Operate, Tasklist) in our development environment.
2. **Process Modeling**: Model the order fulfillment process in BPMN, including happy path and error scenarios.
3. **Service Integration**: Develop job workers in our microservices to handle process tasks.
4. **Kafka Integration**: Implement bidirectional integration between Kafka events and Zeebe workflows.
5. **Testing**: Thoroughly test the integrated solution, including error scenarios and recovery mechanisms.
6. **Monitoring Setup**: Configure monitoring and alerting for the workflow engine.

### Consequences

#### Positive:
- Improved visibility into business processes
- Better handling of complex error scenarios
- Enhanced scalability for high-throughput processing
- Clearer separation between process orchestration and service implementation

#### Negative:
- Steeper learning curve for team members compared to Camunda 7
- Additional infrastructure components to maintain
- Potential complexity in debugging across process engine and services
- Less mature documentation and community resources compared to Camunda 7
- Initial development may be slower as the team builds expertise

