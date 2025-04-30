package com.example.service;

import com.example.dto.ItemDto;

public interface FactoryStockService {

    void periodicReplenishment();

    void onItemRequest(ItemDto request);

}
