package com.example.demodatn2.controller;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.repository.VaiTroRepository;
import com.example.demodatn2.service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminTaiKhoanController {

    private final TaiKhoanService taiKhoanService;
    private final VaiTroRepository vaiTroRepository;

    @GetMapping
    public String listUsers(@RequestParam(required = false) String keyword, 
                           @RequestParam(required = false) String trangThai,
                           Model model) {
        model.addAttribute("users", taiKhoanService.searchTaiKhoans(keyword, trangThai));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTrangThai", trangThai);
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        model.addAttribute("user", taiKhoanService.getTaiKhoanById(id));
        model.addAttribute("allRoles", vaiTroRepository.findAll());
        return "admin/user-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute TaiKhoanDTO userDTO, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.updateTaiKhoan(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Integer id) {
        try {
            taiKhoanService.deleteTaiKhoan(id);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
