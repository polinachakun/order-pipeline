//package com.example.worker;
//
//import com.example.dto.OrderDto;
//import com.example.model.Order;
//import com.example.service.OrderService;
//import io.camunda.zeebe.client.api.response.ActivatedJob;
//import io.camunda.zeebe.client.api.worker.JobClient;
//import io.camunda.zeebe.spring.client.annotation.JobWorker;
//import io.camunda.zeebe.spring.client.annotation.Variable;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OrderServiceWorker {
//
//    private final OrderService orderService;
//
//    @JobWorker(type = "createOrder")
//    public void createOrder(final JobClient client, final ActivatedJob job,
//                            @Variable String orderId,
//                            @Variable String customerId,
//                            @Variable OrderDto orderDto) {
//        log.info("Creating order: {}", orderId);
//
//        try {
//            OrderDto order = orderService.createOrder(orderDto);
//
//            client.newCompleteCommand(job.getKey())
//                    .variables(Map.of(
//                            "orderId", order.getOrderId(),
//                            "orderStatus", "CREATED",
//                            "items", order.getRequestedItems()
//                    ))
//                    .send()
//                    .join();
//
//            log.info("Order created successfully: {}", orderId);
//        } catch (Exception e) {
//            log.error("Failed to create order: {}", orderId, e);
//            client.newFailCommand(job.getKey())
//                    .retries(job.getRetries() - 1)
//                    .errorMessage(e.getMessage())
//                    .send()
//                    .join();
//        }
//    }
//
//    @JobWorker(type = "cancelOrder")
//    public void cancelOrder(final JobClient client, final ActivatedJob job,
//                            @Variable String orderId) {
//        log.info("Compensating - canceling order: {}", orderId);
//
//        try {
////            orderService.cancelOrder(orderId);
//
//            client.newCompleteCommand(job.getKey())
//                    .send()
//                    .join();
//
//            log.info("Order canceled successfully: {}", orderId);
//        } catch (Exception e) {
//            log.error("Failed to cancel order: {}", orderId, e);
//            client.newFailCommand(job.getKey())
//                    .retries(0)  // No retries for compensation
//                    .errorMessage(e.getMessage())
//                    .send()
//                    .join();
//        }
//    }
//}
//
