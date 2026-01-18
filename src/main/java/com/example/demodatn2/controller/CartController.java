package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final DanhMucService danhMucService;
    private final VoucherService voucherService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        String sessionId = session.getId(); // Force session creation
        List<CartItemDTO> items = cartService.getCartItems(session);
        BigDecimal total = cartService.getTotalAmount(items);
        
        String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
        BigDecimal discount = BigDecimal.ZERO;
        
        if (voucherCode != null) {
            var voucherOpt = voucherService.validateVoucher(voucherCode, total);
            if (voucherOpt.isPresent()) {
                discount = voucherService.calculateDiscount(voucherOpt.get(), total);
                session.setAttribute("DISCOUNT_AMOUNT", discount);
            } else {
                session.removeAttribute("APPLIED_VOUCHER_CODE");
                session.removeAttribute("DISCOUNT_AMOUNT");
            }
        }
        
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("discount", discount);
        model.addAttribute("totalAfterDiscount", total.subtract(discount));
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("availableVouchers", voucherService.getAvailableVouchers());
        return "cart";
    }

    @PostMapping("/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucher(@RequestParam String code, HttpSession session) {
        try {
            List<CartItemDTO> items = cartService.getCartItems(session);
            BigDecimal total = cartService.getTotalAmount(items);
            
            var voucherOpt = voucherService.validateVoucher(code, total);
            if (voucherOpt.isPresent()) {
                var voucher = voucherOpt.get();
                BigDecimal discount = voucherService.calculateDiscount(voucher, total);
                session.setAttribute("APPLIED_VOUCHER_CODE", code);
                session.setAttribute("DISCOUNT_AMOUNT", discount);
                
                return Map.of(
                    "success", true,
                    "message", "Áp dụng mã giảm giá thành công",
                    "discount", discount,
                    "totalAfterDiscount", total.subtract(discount)
                );
            } else {
                return Map.of("success", false, "message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Integer bienTheId, 
                                        @RequestParam Integer soLuong, 
                                        HttpSession session) {
        String sessionId = session.getId(); // Force session creation
        try {
            cartService.addToCart(bienTheId, soLuong, session);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            return Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "count", count);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Integer itemId, 
                                             @RequestParam Integer soLuong, 
                                             HttpSession session) {
        try {
            cartService.updateQuantity(itemId, soLuong);
            List<CartItemDTO> items = cartService.getCartItems(session);
            BigDecimal total = cartService.getTotalAmount(items);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);
            
            // Recalculate discount if a voucher was applied
            String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
            BigDecimal discount = BigDecimal.ZERO;
            if (voucherCode != null) {
                var voucherOpt = voucherService.validateVoucher(voucherCode, total);
                if (voucherOpt.isPresent()) {
                    discount = voucherService.calculateDiscount(voucherOpt.get(), total);
                    session.setAttribute("DISCOUNT_AMOUNT", discount);
                } else {
                    // If the voucher is no longer valid (e.g., total below minimum)
                    session.removeAttribute("APPLIED_VOUCHER_CODE");
                    session.removeAttribute("DISCOUNT_AMOUNT");
                }
            }

            return Map.of(
                "success", true, 
                "total", total,
                "discount", discount,
                "totalAfterDiscount", total.subtract(discount),
                "count", count
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam Integer itemId, HttpSession session) {
        try {
            cartService.removeItem(itemId);
            List<CartItemDTO> items = cartService.getCartItems(session);
            BigDecimal total = cartService.getTotalAmount(items);
            int count = cartService.getItemCount(session);
            session.setAttribute("CART_COUNT", count);

            // Recalculate discount if a voucher was applied
            String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
            BigDecimal discount = BigDecimal.ZERO;
            if (voucherCode != null) {
                var voucherOpt = voucherService.validateVoucher(voucherCode, total);
                if (voucherOpt.isPresent()) {
                    discount = voucherService.calculateDiscount(voucherOpt.get(), total);
                    session.setAttribute("DISCOUNT_AMOUNT", discount);
                } else {
                    session.removeAttribute("APPLIED_VOUCHER_CODE");
                    session.removeAttribute("DISCOUNT_AMOUNT");
                }
            }

            return Map.of(
                "success", true, 
                "total", total,
                "discount", discount,
                "totalAfterDiscount", total.subtract(discount),
                "count", count
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
