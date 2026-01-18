package com.example.demodatn2.config;

import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.VaiTro;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Khởi tạo vai trò
            if (vaiTroRepository.findByMa("ADMIN").isEmpty()) {
                VaiTro adminRole = new VaiTro();
                adminRole.setMa("ADMIN");
                adminRole.setTen("Quản trị viên");
                vaiTroRepository.save(adminRole);
            }
            if (vaiTroRepository.findByMa("USER").isEmpty()) {
                VaiTro userRole = new VaiTro();
                userRole.setMa("USER");
                userRole.setTen("Người dùng");
                vaiTroRepository.save(userRole);
            }
            if (vaiTroRepository.findByMa("NHAN_VIEN").isEmpty()) {
                VaiTro staffRole = new VaiTro();
                staffRole.setMa("NHAN_VIEN");
                staffRole.setTen("Nhân viên");
                vaiTroRepository.save(staffRole);
            }

            VaiTro adminRole = vaiTroRepository.findByMa("ADMIN").get();
            VaiTro userRole = vaiTroRepository.findByMa("USER").get();
            VaiTro staffRole = vaiTroRepository.findByMa("NHAN_VIEN").get();

            // Khởi tạo tài khoản
            if (taiKhoanRepository.findByTenDangNhap("admin").isEmpty()) {
                TaiKhoan admin = new TaiKhoan();
                admin.setTenDangNhap("admin");
                admin.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                admin.setHoTen("Quản trị viên");
                admin.setEmail("admin@hancos.com");
                admin.setTrangThai("ACTIVE");
                admin.setVaiTros(Set.of(adminRole));
                taiKhoanRepository.save(admin);
            }

            if (taiKhoanRepository.findByTenDangNhap("nhanvien").isEmpty() && taiKhoanRepository.findByEmail("nhanvien@gmail.com").isEmpty()) {
                TaiKhoan staff = new TaiKhoan();
                staff.setTenDangNhap("nhanvien");
                staff.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                staff.setHoTen("Nhân viên demo");
                staff.setEmail("nhanvien@gmail.com");
                staff.setTrangThai("ACTIVE");
                staff.setVaiTros(Set.of(staffRole));
                taiKhoanRepository.save(staff);
            }

            if (taiKhoanRepository.findByTenDangNhap("user").isEmpty()) {
                TaiKhoan user = new TaiKhoan();
                user.setTenDangNhap("user");
                user.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                user.setHoTen("Người dùng demo");
                user.setEmail("user@gmail.com");
                user.setTrangThai("ACTIVE");
                user.setVaiTros(Set.of(userRole));
                taiKhoanRepository.save(user);
            }
        };
    }
}
