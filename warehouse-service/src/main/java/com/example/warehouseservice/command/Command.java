package com.example.warehouseservice.command;

import com.example.warehouseservice.dto.AbstractDto;

public interface Command {
    void execute(AbstractDto payload);
}
