package com.example.demodatn2.controller;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.service.DanhMucService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminDanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", danhMucService.getAllDTOs());
        return "admin/categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new DanhMucDTO());
        model.addAttribute("parents", danhMucService.getParents());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        DanhMuc dm = danhMucService.getById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + id));
        
        DanhMucDTO dto = DanhMucDTO.builder()
                .id(dm.getId())
                .ma(dm.getMa())
                .ten(dm.getTen())
                .trangThai(dm.getTrangThai())
                .danhMucChaId(dm.getDanhMucCha() != null ? dm.getDanhMucCha().getId() : null)
                .build();
                
        model.addAttribute("category", dto);
        model.addAttribute("parents", danhMucService.getParents());
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute DanhMucDTO categoryDTO, RedirectAttributes redirectAttributes) {
        try {
            danhMucService.saveDTO(categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/add";
        }
        return "redirect:/admin/categories";
    }
}
