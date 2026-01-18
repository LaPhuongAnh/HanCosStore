package com.example.demodatn2.controller;

import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;

    @GetMapping("/them-san-pham")
    public String showAddProductPage(Model model) {
        model.addAttribute("parentDanhMuc", danhMucService.getParents());
        return "addsanpham";
    }

    /**
     * Hiển thị trang danh sách sản phẩm dành cho Admin
     */
    @GetMapping("/san-pham")
    public String showProductListPage(@RequestParam(required = false) String keyword, 
                                     @RequestParam(required = false) Integer danhMucId,
                                     @RequestParam(required = false) String trangThai,
                                     Model model) {
        model.addAttribute("products", sanPhamService.searchSanPham(keyword, danhMucId, trangThai));
        model.addAttribute("categories", danhMucService.getActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDanhMucId", danhMucId);
        model.addAttribute("selectedTrangThai", trangThai);
        return "admin/products"; 
    }

    /**
     * Hiển thị trang chi tiết sản phẩm dành cho Admin/Nhân viên (Read-only)
     */
    @GetMapping("/admin/san-pham/view/{id}")
    public String viewProductPage(@PathVariable Integer id, Model model) {
        model.addAttribute("product", sanPhamService.getSanPhamById(id));
        return "admin/product-detail";
    }

    /**
     * Hiển thị trang chỉnh sửa sản phẩm
     */
    @GetMapping("/admin/san-pham/edit/{id}")
    public String showEditProductPage(@PathVariable Integer id, Model model) {
        model.addAttribute("product", sanPhamService.getSanPhamById(id));
        model.addAttribute("parentDanhMuc", danhMucService.getParents());
        return "editsanpham";
    }
}
