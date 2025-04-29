package com.example.warehouseservice.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final InMemoryCommandRetryRepository retryRepository;
    private final StatefulRetryCommandHandler retryCommandHandler;

    @Scheduled(fixedDelay = 10000)
    public void retryPendingCommands() {
        List<CommandRetryEntity> pendingCommands = retryRepository.findByStatusAndNextRetryAtBefore(
                CommandRetryEntity.RetryStatus.PENDING,
                LocalDateTime.now()
        );

    //    log.info("Scheduler found {} commands to retry", pendingCommands.size());

        for (CommandRetryEntity retryEntity : pendingCommands) {
            log.info("Retrying command id {}", retryEntity.getId());
            retryCommandHandler.executeWithRetry(retryEntity);
        }
    }
}
