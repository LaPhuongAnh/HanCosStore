package com.example.demodatn2.controller;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.getAll());
        return "admin/vouchers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new MaGiamGia());
        return "admin/voucher-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        MaGiamGia voucher = voucherService.getById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MaGiamGia voucher, 
                       @RequestParam("batDauStr") String batDauStr,
                       @RequestParam("ketThucStr") String ketThucStr,
                       RedirectAttributes redirectAttributes) {
        try {
            // Chuyển đổi từ String (datetime-local) sang Instant
            if (!batDauStr.isEmpty()) {
                voucher.setBatDauLuc(LocalDateTime.parse(batDauStr).atZone(ZoneId.systemDefault()).toInstant());
            }
            if (!ketThucStr.isEmpty()) {
                voucher.setKetThucLuc(LocalDateTime.parse(ketThucStr).atZone(ZoneId.systemDefault()).toInstant());
            }
            
            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/vouchers/add";
        }
        return "redirect:/admin/vouchers";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id) {
        try {
            voucherService.delete(id);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
