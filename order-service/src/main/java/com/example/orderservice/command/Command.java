package com.example.orderservice.command;

import com.example.orderservice.dto.AbstractDto;

public interface Command {
    void execute(AbstractDto payload);
}
