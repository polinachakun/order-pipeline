package com.example.warehouseservice.retry;

import com.example.warehouseservice.command.Command;
import com.example.warehouseservice.command.DeliveryCommandServiceImpl;
import com.example.warehouseservice.command.dto.StartDeliveryCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatefulRetryCommandHandler {

    private static final int MAX_RETRIES = 10;

    private final InMemoryCommandRetryRepository retryRepository;
    private final Command deliveryCommandService;

    public void handle(StartDeliveryCommand commandPayload) {
        CommandRetryEntity retryEntity = new CommandRetryEntity(
                UUID.randomUUID().toString(),
                "StartDeliveryCommand",
                commandPayload,
                CommandRetryEntity.RetryStatus.PENDING,
                0,
                LocalDateTime.now(),
                null
        );
        retryRepository.save(retryEntity);
        executeWithRetry(retryEntity);
    }

    public void executeWithRetry(CommandRetryEntity retryEntity) {
        try {
            retryEntity.setStatus(CommandRetryEntity.RetryStatus.IN_PROGRESS);
            retryEntity.setLastAttemptAt(LocalDateTime.now());
            retryRepository.save(retryEntity);
            log.info("Execute delivery command: {}", retryEntity.getPayload());
            deliveryCommandService.execute(retryEntity.getPayload());

            retryEntity.setStatus(CommandRetryEntity.RetryStatus.COMPLETED);
            retryRepository.save(retryEntity);

        } catch (Exception e) {
            if (retryEntity.getRetryCount() >= MAX_RETRIES) {
                log.error("Command {} failed after {} retries", retryEntity.getId(), MAX_RETRIES, e);
                retryEntity.setStatus(CommandRetryEntity.RetryStatus.FAILED);
            } else {
                log.warn("Retry #{} for command {} failed: {}", retryEntity.getRetryCount(), retryEntity.getId(), e.getMessage());
                retryEntity.setRetryCount(retryEntity.getRetryCount() + 1);
                retryEntity.setStatus(CommandRetryEntity.RetryStatus.PENDING);
                retryEntity.setNextRetryAt(calculateNextRetryTime(retryEntity.getRetryCount()));
            }
            retryRepository.save(retryEntity);
        }
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        int delaySeconds = (int) Math.min(60, Math.pow(2, retryCount));
        return LocalDateTime.now().plusSeconds(delaySeconds);
//        return LocalDateTime.now().plusSeconds(10);
    }
}

