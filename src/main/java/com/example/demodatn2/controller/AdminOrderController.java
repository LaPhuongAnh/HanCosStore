package com.example.demodatn2.controller;

import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.service.OrderService;
import com.example.demodatn2.repository.YeuCauDoiTraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final YeuCauDoiTraRepository yeuCauDoiTraRepository;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status, 
                             @RequestParam(required = false) String keyword, 
                             Model model) {
        List<DonHang> orders = orderService.searchOrders(keyword, status);
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatusCounts", orderService.getOrderStatusCounts());
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("keyword", keyword);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        model.addAttribute("items", orderService.getOrderItems(id));
        orderService.getReturnRequest(id).ifPresent(r -> model.addAttribute("returnRequest", r));
        return "admin/order-detail";
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, reason, true);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/return/{requestId}/approve")
    public String approveReturn(@PathVariable Integer requestId, RedirectAttributes redirectAttributes) {
        try {
            var yeuCau = yeuCauDoiTraRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu."));
            orderService.approveReturnRequest(requestId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt yêu cầu trả hàng.");
            return "redirect:/admin/orders/" + yeuCau.getDonHang().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/return/{requestId}/reject")
    public String rejectReturn(@PathVariable Integer requestId, @RequestParam String reason, RedirectAttributes redirectAttributes) {
        try {
            var yeuCau = yeuCauDoiTraRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu."));
            orderService.rejectReturnRequest(requestId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu trả hàng.");
            return "redirect:/admin/orders/" + yeuCau.getDonHang().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }
}
