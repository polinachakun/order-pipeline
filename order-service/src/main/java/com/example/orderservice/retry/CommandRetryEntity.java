package com.example.orderservice.retry;

import com.example.orderservice.dto.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandRetryEntity {
    private String id;
    private String commandType;
    private AbstractDto payload;
    private RetryStatus status;
    private int retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime lastAttemptAt;

    public enum RetryStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}


