package com.example.demodatn2.controller;

import com.example.demodatn2.dto.DanhMucDTO;
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
    public String banHangTaiQuay(Model model) {
        model.addAttribute("customers", taiKhoanService.searchTaiKhoans(null, "ACTIVE"));
        model.addAttribute("products", sanPhamService.searchSanPham(null, null, "ACTIVE"));
        model.addAttribute("categories", danhMucService.getAllDTOs());
        return "admin/banhangtaiquay";
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

    @PostMapping("/pos/api/checkout")
    @ResponseBody
    public Map<String, Object> posCheckout(@RequestBody PosOrderRequestDTO req, HttpSession session) {
        try {
            TaiKhoanDTO staff = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
            DonHang donHang = orderService.createPosOrder(req, staff);
            return Map.of("success", true, "orderId", donHang.getId(), "orderCode", donHang.getMaDonHang(),
                    "total", donHang.getTongTien());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
