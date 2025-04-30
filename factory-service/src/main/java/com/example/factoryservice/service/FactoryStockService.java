package com.example.factoryservice.service;

import com.example.factoryservice.dto.ItemDto;

public interface FactoryStockService {

    void periodicReplenishment();

    void onItemRequest(ItemDto request);

}
