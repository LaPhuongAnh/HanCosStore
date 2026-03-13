package com.example.demodatn2.controller;

import com.example.demodatn2.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AdminSidebarModelAdvice {

    private final OrderService orderService;

    @ModelAttribute("adminPendingOrderCount")
    public long adminPendingOrderCount(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/admin")) {
            return 0L;
        }
        return orderService.getPendingConfirmationCount();
    }
}
