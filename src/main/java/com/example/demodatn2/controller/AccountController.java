package com.example.demodatn2.controller;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.service.DanhMucService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final DanhMucService danhMucService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        TaiKhoanDTO dto = TaiKhoanDTO.builder()
                .id(user.getId())
                .tenDangNhap(user.getTenDangNhap())
                .hoTen(user.getHoTen())
                .email(user.getEmail())
                .soDienThoai(user.getSoDienThoai())
                .ngayTao(user.getNgayTao())
                .build();

        model.addAttribute("user", dto);
        model.addAttribute("categories", danhMucService.getActive());
        
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(HttpSession session, Model model) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        model.addAttribute("user", user);
        model.addAttribute("categories", danhMucService.getActive());
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String hoTen,
                                @RequestParam String email,
                                @RequestParam String soDienThoai,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return "redirect:/login";
        }

        TaiKhoan user = taiKhoanRepository.findById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setHoTen(hoTen);
        user.setEmail(email);
        user.setSoDienThoai(soDienThoai);

        taiKhoanRepository.save(user);

        // Cập nhật lại session
        loginUser.setHoTen(user.getHoTen());
        loginUser.setEmail(user.getEmail());
        loginUser.setSoDienThoai(user.getSoDienThoai());
        session.setAttribute("LOGIN_USER", loginUser);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/account/profile";
    }
}
