////package com.example.factoryservice.camunda;
//
//import com.example.factoryservice.dto.ItemDto;
//import com.example.factoryservice.service.FactoryStockService;
//import io.camunda.zeebe.client.api.response.ActivatedJob;
//import io.camunda.zeebe.client.api.worker.JobClient;
//import io.camunda.zeebe.spring.client.annotation.JobWorker;
//import io.camunda.zeebe.spring.client.annotation.Variable;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class FactoryServiceWorker {
//
//    private final FactoryStockService factoryService;
//
//    @JobWorker(type = "requestFromFactory")
//    public void requestFromFactory(final JobClient client, final ActivatedJob job,
//                                   @Variable String orderId,
//                                   @Variable List<ItemDto> items) {
//        log.info("Requesting items from factory for order: {}", orderId);
//
//        try {
//            // Initiate production or transfer from factory
//            String productionOrderId = factoryService.requestProduction(orderId, items);
//
//            // In a real scenario, this might be asynchronous
//            // For simplicity, we'll wait for production to complete
//            factoryService.waitForProductionCompletion(productionOrderId);
//
//            // Transfer items to warehouse
//            factoryService.transferToWarehouse(productionOrderId);
//
//            client.newCompleteCommand(job.getKey())
//                    .variables(Map.of(
//                            "productionOrderId", productionOrderId,
//                            "productionStatus", "COMPLETED_AND_TRANSFERRED"
//                    ))
//                    .send()
//                    .join();
//
//            log.info("Items produced and transferred to warehouse for order: {}", orderId);
//        } catch (Exception e) {
//            log.error("Failed to produce items for order: {}", orderId, e);
//            client.newFailCommand(job.getKey())
//                    .retries(job.getRetries() - 1)
//                    .errorMessage(e.getMessage())
//                    .send()
//                    .join();
//        }
//    }
//
//    // Note: No compensation needed for factory as production cannot be undone
//    // If items are produced but not needed, they go back to general inventory
//}