package com.example.orderservice.orchestration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrchestrationStrategyFactory {

    private final Map<OrchestrationMode, OrchestrationStrategy> strategies;
    private final OrchestrationMode defaultMode;

    public OrchestrationStrategyFactory(
            List<OrchestrationStrategy> strategyList,
            @Value("${orchestration.mode:CAMUNDA}") OrchestrationMode defaultMode) {

        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        OrchestrationStrategy::getMode,
                        Function.identity()
                ));
        this.defaultMode = defaultMode;
    }

    public OrchestrationStrategy getStrategy() {
        return strategies.get(defaultMode);
    }

    public OrchestrationStrategy getStrategy(OrchestrationMode mode) {
        return strategies.get(mode);
    }
}