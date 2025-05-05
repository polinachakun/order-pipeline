//package com.example.camunda;
//
//import io.camunda.zeebe.client.ZeebeClient;
//import io.camunda.zeebe.spring.client.EnableZeebeClient;
//import io.camunda.zeebe.spring.client.annotation.Deployment;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.event.EventListener;
//
//@Slf4j
//@Configuration
//public class CamundaCloudConfig {
//
//    @Value("${camunda.client.cluster-id}")
//    private String clusterId;
//
//    @Value("${camunda.client.auth.client-id}")
//    private String clientId;
//
//    @Value("${camunda.client.auth.client-secret}")
//    private String clientSecret;
//
//    @Value("${camunda.client.region}")
//    private String region;
//
//    @Bean
//    public ZeebeClient zeebeClient() {
//        // Create the client using the Cloud Builder
//        return ZeebeClient.newCloudClientBuilder()
//                .withClusterId(clusterId)
//                .withClientId(clientId)
//                .withClientSecret(clientSecret)
//                .withRegion(region)
//                .build();
//    }
//}