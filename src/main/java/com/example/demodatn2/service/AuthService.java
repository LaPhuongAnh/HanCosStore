package com.example.demodatn2.service;

import com.example.demodatn2.dto.RegisterRequestDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.VaiTro;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.repository.VaiTroRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Service xử lý đăng ký, đăng nhập và quản lý session đăng nhập.
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;

    @Transactional
    public void register(RegisterRequestDTO request) {
        if (taiKhoanRepository.findByTenDangNhap(request.getTenDangNhap()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(request.getTenDangNhap());
        taiKhoan.setMatKhau(BCrypt.hashpw(request.getMatKhau(), BCrypt.gensalt()));
        taiKhoan.setHoTen(request.getHoTen());
        taiKhoan.setEmail(request.getEmail());
        taiKhoan.setSoDienThoai(request.getSoDienThoai());
        taiKhoan.setTrangThai("ACTIVE");

        // Gán vai trò mặc định là CUSTOMER
        VaiTro customerRole = vaiTroRepository.findByMa("CUSTOMER")
            .orElseThrow(() -> new RuntimeException("Vai trò CUSTOMER không tồn tại!"));
        taiKhoan.setVaiTros(new HashSet<>(Collections.singletonList(customerRole)));

        taiKhoanRepository.save(taiKhoan);
    }

    public boolean login(String tenDangNhap, String matKhau, HttpSession session) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return false;
        }

        String loginValue = tenDangNhap.trim();
        Optional<TaiKhoan> optUser = taiKhoanRepository.findByTenDangNhap(loginValue);
        if (optUser.isEmpty()) {
            optUser = taiKhoanRepository.findByEmailIgnoreCase(loginValue);
        }
        
        if (optUser.isPresent()) {
            TaiKhoan user = optUser.get();
            
            if (!"ACTIVE".equals(user.getTrangThai())) {
                return false;
            }

            boolean isMatch = false;
            String hashed = user.getMatKhau();

            // Tự nhận biết BCrypt: nếu hash bắt đầu bằng $2a$ hoặc $2b$
            if (hashed != null && (hashed.startsWith("$2a$") || hashed.startsWith("$2b$"))) {
                try {
                    isMatch = BCrypt.checkpw(matKhau, hashed);
                } catch (Exception e) {
                    isMatch = false;
                }
            } else {
                // So sánh trực tiếp cho plain text
                isMatch = matKhau.equals(hashed);
            }

            if (isMatch) {
                TaiKhoanDTO dto = TaiKhoanDTO.builder()
                        .id(user.getId())
                        .tenDangNhap(user.getTenDangNhap())
                        .hoTen(user.getHoTen())
                        .email(user.getEmail())
                        .soDienThoai(user.getSoDienThoai())
                        .build();

                List<String> roles = user.getVaiTros().stream()
                        .map(VaiTro::getMa)
                        .filter(ma -> ma.equals("ADMIN") || ma.equals("STAFF") || ma.equals("CUSTOMER"))
                        .collect(Collectors.toList());

                session.setAttribute("LOGIN_USER", dto);
                session.setAttribute("ROLES", roles);
                return true;
            }
        }
        return false;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
