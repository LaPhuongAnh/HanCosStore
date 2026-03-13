package com.example.demodatn2.controller;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.dto.PosCartItemDTO;
import com.example.demodatn2.dto.PosCartItemRequestDTO;
import com.example.demodatn2.dto.PosOrderRequestDTO;
import com.example.demodatn2.dto.SanPhamResponseDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ThongKeService thongKeService;
    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;
    private final OrderService orderService;
    private final TaiKhoanService taiKhoanService;
    private final VoucherService voucherService;
    private final PosCartService posCartService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", thongKeService.getDoanhThuTongHop());
        return "admin/dashboard";
    }

    @GetMapping("/pos")
    public String pos() {
        return "admin/pos";
    }

    @GetMapping("/ban-hang-tai-quay")
    public String banHangTaiQuay(Model model, HttpSession session) {
        model.addAttribute("customers", taiKhoanService.searchTaiKhoans(null, "ACTIVE"));
        model.addAttribute("products", sanPhamService.searchSanPham(null, null, "ACTIVE"));
        model.addAttribute("categories", danhMucService.getAllDTOs());
        model.addAttribute("posInvoices", posCartService.listInvoices(session));
        model.addAttribute("posActiveInvoice", posCartService.getActiveInvoiceId(session));
        model.addAttribute("posSessionCart", posCartService.getCart(session));
        return "admin/banhangtaiquay";
    }

    @GetMapping("/pos/api/cart")
    @ResponseBody
    public Map<String, Object> posCart(HttpSession session) {
        return Map.of("success", true, "cart", posCartService.getCart(session));
    }

    @PostMapping("/pos/api/cart/items")
    @ResponseBody
    public Map<String, Object> addPosCartItem(@RequestBody PosCartItemRequestDTO req, HttpSession session) {
        try {
            List<PosCartItemDTO> cart = posCartService.addItem(session, req.getVariantId(), req.getQty());
            return Map.of("success", true, "cart", cart);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/pos/api/cart/items/{variantId}")
    @ResponseBody
    public Map<String, Object> updatePosCartItem(@PathVariable Integer variantId,
                                                 @RequestBody PosCartItemRequestDTO req,
                                                 HttpSession session) {
        try {
            List<PosCartItemDTO> cart = posCartService.updateQty(session, variantId, req.getQty());
            return Map.of("success", true, "cart", cart);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/pos/api/cart/items/{variantId}")
    @ResponseBody
    public Map<String, Object> removePosCartItem(@PathVariable Integer variantId, HttpSession session) {
        List<PosCartItemDTO> cart = posCartService.removeItem(session, variantId);
        return Map.of("success", true, "cart", cart);
    }

    @DeleteMapping("/pos/api/cart")
    @ResponseBody
    public Map<String, Object> clearPosCart(HttpSession session) {
        posCartService.clear(session);
        return Map.of("success", true, "cart", List.of());
    }

    @GetMapping("/pos/api/products")
    @ResponseBody
    public List<SanPhamResponseDTO> posProducts() {
        return sanPhamService.searchSanPham(null, null, "ACTIVE");
    }

    @GetMapping("/pos/api/categories")
    @ResponseBody
    public List<DanhMucDTO> posCategories() {
        return danhMucService.getAllDTOs();
    }

    @GetMapping("/pos/api/customers")
    @ResponseBody
    public List<TaiKhoanDTO> searchCustomers(@RequestParam(required = false) String q) {
        return taiKhoanService.searchTaiKhoans(q, "ACTIVE");
    }

    @PostMapping("/pos/api/voucher/validate")
    @ResponseBody
    public Map<String, Object> validatePosVoucher(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        Object amountObj = body.get("amount");
        BigDecimal amount = new BigDecimal(amountObj.toString());

        Optional<MaGiamGia> voucherOpt = voucherService.validateVoucher(code, amount);
        if (voucherOpt.isPresent()) {
            MaGiamGia v = voucherOpt.get();
            BigDecimal discount = voucherService.calculateDiscount(v, amount);
            return Map.of("success", true, "discount", discount, "code", v.getMa(),
                    "label", v.getLoai() + " - " + v.getGiaTri());
        }
        return Map.of("success", false, "message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
    }

    @GetMapping("/pos/api/invoices")
    @ResponseBody
    public Map<String, Object> getInvoices(HttpSession session) {
        return Map.of("success", true,
                "invoices", posCartService.listInvoices(session),
                "activeInvoiceId", posCartService.getActiveInvoiceId(session));
    }

    @PostMapping("/pos/api/invoices")
    @ResponseBody
    public Map<String, Object> createInvoice(HttpSession session) {
        String newId = posCartService.createInvoice(session);
        return Map.of("success", true,
                "invoiceId", newId,
                "invoices", posCartService.listInvoices(session),
                "activeInvoiceId", newId,
                "cart", posCartService.getCart(session));
    }

    @PutMapping("/pos/api/invoices/{invoiceId}/activate")
    @ResponseBody
    public Map<String, Object> activateInvoice(@PathVariable String invoiceId, HttpSession session) {
        try {
            posCartService.switchInvoice(session, invoiceId);
            return Map.of("success", true,
                    "invoices", posCartService.listInvoices(session),
                    "activeInvoiceId", invoiceId,
                    "cart", posCartService.getCart(session));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/pos/api/invoices/{invoiceId}")
    @ResponseBody
    public Map<String, Object> removeInvoice(@PathVariable String invoiceId, HttpSession session) {
        String newActiveId = posCartService.deleteInvoice(session, invoiceId);
        return Map.of("success", true,
                "invoices", posCartService.listInvoices(session),
                "activeInvoiceId", newActiveId,
                "cart", posCartService.getCart(session));
    }

    @PostMapping("/pos/api/checkout")
    @ResponseBody
    public Map<String, Object> posCheckout(@RequestBody PosOrderRequestDTO req, HttpSession session) {
        try {
            List<PosCartItemDTO> cart = posCartService.getCart(session);
            if (cart.isEmpty()) {
                return Map.of("success", false, "message", "Giỏ hàng POS trống");
            }

            List<PosOrderRequestDTO.PosItemDTO> items = new ArrayList<>();
            for (PosCartItemDTO cartItem : cart) {
                PosOrderRequestDTO.PosItemDTO item = new PosOrderRequestDTO.PosItemDTO();
                item.setVariantId(cartItem.getVariantId());
                item.setQty(cartItem.getQty());
                item.setPrice(cartItem.getPrice());
                items.add(item);
            }
            req.setItems(items);

            TaiKhoanDTO staff = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
            DonHang donHang = orderService.createPosOrder(req, staff);
            posCartService.clear(session);
            return Map.of("success", true, "orderId", donHang.getId(), "orderCode", donHang.getMaDonHang(),
                    "total", donHang.getTongTien(), "invoices", posCartService.listInvoices(session));
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
