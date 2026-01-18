package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final DanhMucService danhMucService;
    private final TaiKhoanRepository taiKhoanRepository;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        List<CartItemDTO> items = cartService.getCartItems(session);
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        BigDecimal total = cartService.getTotalAmount(items);
        
        // Luôn tính toán lại voucher khi vào trang thanh toán để đảm bảo chính xác nhất
        String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
        BigDecimal discount = BigDecimal.ZERO;
        
        if (voucherCode != null) {
            var voucherOpt = orderService.getVoucherService().validateVoucher(voucherCode, total);
            if (voucherOpt.isPresent()) {
                discount = orderService.getVoucherService().calculateDiscount(voucherOpt.get(), total);
                session.setAttribute("DISCOUNT_AMOUNT", discount);
            } else {
                session.removeAttribute("APPLIED_VOUCHER_CODE");
                session.removeAttribute("DISCOUNT_AMOUNT");
                model.addAttribute("voucherWarning", "Mã giảm giá đã bị gỡ do không còn đủ điều kiện.");
            }
        }
        
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("discount", discount);
        model.addAttribute("finalTotal", total.subtract(discount));
        model.addAttribute("user", loginUser);
        model.addAttribute("categories", danhMucService.getActive());
        
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String hoTen,
                             @RequestParam String soDienThoai,
                             @RequestParam String email,
                             @RequestParam String diaChi,
                             @RequestParam(required = false) String ghiChu,
                             @RequestParam(defaultValue = "COD") String payment,
                             HttpSession session,
                             Model model) {
        try {
            DonHang donHang = orderService.createOrder(hoTen, soDienThoai, email, diaChi, ghiChu, payment, session);
            session.setAttribute("CART_COUNT", 0);
            return "redirect:/order/success?id=" + donHang.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return checkout(session, model);
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam Integer id, Model model) {
        DonHang order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("categories", danhMucService.getActive());
        return "order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }
        
        TaiKhoan taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElseThrow();
        List<DonHang> orders = orderService.getOrdersByAccount(taiKhoan);
        
        model.addAttribute("orders", orders);
        model.addAttribute("categories", danhMucService.getActive());
        return "my-orders";
    }

    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable Integer id, HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        DonHang order = orderService.getOrderById(id);
        // Bảo mật: chỉ cho xem đơn hàng của chính mình
        if (!order.getTaiKhoan().getId().equals(loginUser.getId())) {
            return "redirect:/order/my-orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", orderService.getOrderItems(id));
        model.addAttribute("categories", danhMucService.getActive());
        return "my-order-detail";
    }

    @PostMapping("/cancel/{id}")
    @ResponseBody
    public String cancelOrder(@PathVariable Integer id, @RequestParam String reason, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho hủy đơn hàng của chính mình
            if (!order.getTaiKhoan().getId().equals(loginUser.getId())) {
                return "Bạn không có quyền hủy đơn hàng này.";
            }

            orderService.cancelOrder(id, reason, false);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/update-address/{id}")
    @ResponseBody
    public String updateAddress(@PathVariable Integer id,
                                @RequestParam String hoTen,
                                @RequestParam String soDienThoai,
                                @RequestParam String diaChi,
                                HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "Bạn cần đăng nhập để thực hiện thao tác này.";
        }

        try {
            DonHang order = orderService.getOrderById(id);
            // Bảo mật: chỉ cho sửa đơn hàng của chính mình
            if (order.getTaiKhoan() == null || !order.getTaiKhoan().getId().equals(loginUser.getId())) {
                return "Bạn không có quyền chỉnh sửa đơn hàng này.";
            }

            orderService.updateOrderAddress(id, hoTen, soDienThoai, diaChi);
            return "SUCCESS";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
