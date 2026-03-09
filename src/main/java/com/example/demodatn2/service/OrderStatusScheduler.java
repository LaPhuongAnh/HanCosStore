package com.example.demodatn2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "0 0 2 * * *")
    public void autoCompleteDeliveredOrders() {
        orderService.autoCompleteDeliveredOrders(7);
    }
}
