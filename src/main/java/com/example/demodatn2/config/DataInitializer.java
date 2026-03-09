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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Chuẩn hóa vai trò hệ thống (ADMIN / STAFF / CUSTOMER)
            Optional<VaiTro> legacyUser = vaiTroRepository.findByMa("USER");
            Optional<VaiTro> legacyStaff = vaiTroRepository.findByMa("NHAN_VIEN");
            Optional<VaiTro> existingCustomer = vaiTroRepository.findByMa("CUSTOMER");
            Optional<VaiTro> existingStaff = vaiTroRepository.findByMa("STAFF");

            if (legacyUser.isPresent() && existingCustomer.isEmpty()) {
                VaiTro customerRole = legacyUser.get();
                customerRole.setMa("CUSTOMER");
                customerRole.setTen("Khách hàng");
                vaiTroRepository.save(customerRole);
                existingCustomer = Optional.of(customerRole);
            }

            if (legacyStaff.isPresent() && existingStaff.isEmpty()) {
                VaiTro staffRole = legacyStaff.get();
                staffRole.setMa("STAFF");
                staffRole.setTen("Nhân viên");
                vaiTroRepository.save(staffRole);
                existingStaff = Optional.of(staffRole);
            }

            if (legacyUser.isPresent() && existingCustomer.isPresent()
                    && !legacyUser.get().getId().equals(existingCustomer.get().getId())) {
                VaiTro oldRole = legacyUser.get();
                VaiTro newRole = existingCustomer.get();
                taiKhoanRepository.findAll().forEach(taiKhoan -> {
                    if (taiKhoan.getVaiTros().contains(oldRole)) {
                        Set<VaiTro> roles = new HashSet<>(taiKhoan.getVaiTros());
                        roles.remove(oldRole);
                        roles.add(newRole);
                        taiKhoan.setVaiTros(roles);
                        taiKhoanRepository.save(taiKhoan);
                    }
                });
                vaiTroRepository.delete(oldRole);
            }

            if (legacyStaff.isPresent() && existingStaff.isPresent()
                    && !legacyStaff.get().getId().equals(existingStaff.get().getId())) {
                VaiTro oldRole = legacyStaff.get();
                VaiTro newRole = existingStaff.get();
                taiKhoanRepository.findAll().forEach(taiKhoan -> {
                    if (taiKhoan.getVaiTros().contains(oldRole)) {
                        Set<VaiTro> roles = new HashSet<>(taiKhoan.getVaiTros());
                        roles.remove(oldRole);
                        roles.add(newRole);
                        taiKhoan.setVaiTros(roles);
                        taiKhoanRepository.save(taiKhoan);
                    }
                });
                vaiTroRepository.delete(oldRole);
            }

            VaiTro adminRole = vaiTroRepository.findByMa("ADMIN")
                    .orElseGet(() -> {
                        VaiTro role = new VaiTro();
                        role.setMa("ADMIN");
                        role.setTen("Quản trị viên");
                        return vaiTroRepository.save(role);
                    });

            VaiTro staffRole = vaiTroRepository.findByMa("STAFF")
                    .orElseGet(() -> {
                        VaiTro role = new VaiTro();
                        role.setMa("STAFF");
                        role.setTen("Nhân viên");
                        return vaiTroRepository.save(role);
                    });

            VaiTro customerRole = vaiTroRepository.findByMa("CUSTOMER")
                    .orElseGet(() -> {
                        VaiTro role = new VaiTro();
                        role.setMa("CUSTOMER");
                        role.setTen("Khách hàng");
                        return vaiTroRepository.save(role);
                    });

            // Khởi tạo tài khoản
            if (taiKhoanRepository.findByTenDangNhap("admin").isEmpty()) {
                TaiKhoan admin = new TaiKhoan();
                admin.setTenDangNhap("admin");
                admin.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                admin.setHoTen("Quản trị viên");
                admin.setEmail("admin@hancos.com");
                admin.setTrangThai("ACTIVE");
                admin.setVaiTros(Set.of(adminRole, customerRole));
                taiKhoanRepository.save(admin);
            }

            if (taiKhoanRepository.findByTenDangNhap("nhanvien").isEmpty() && taiKhoanRepository.findByEmail("nhanvien@gmail.com").isEmpty()) {
                TaiKhoan staff = new TaiKhoan();
                staff.setTenDangNhap("nhanvien");
                staff.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                staff.setHoTen("Nhân viên demo");
                staff.setEmail("nhanvien@gmail.com");
                staff.setTrangThai("ACTIVE");
                staff.setVaiTros(Set.of(staffRole, customerRole));
                taiKhoanRepository.save(staff);
            }

            if (taiKhoanRepository.findByTenDangNhap("user").isEmpty()) {
                TaiKhoan user = new TaiKhoan();
                user.setTenDangNhap("user");
                user.setMatKhau(BCrypt.hashpw("123456", BCrypt.gensalt()));
                user.setHoTen("Người dùng demo");
                user.setEmail("user@gmail.com");
                user.setTrangThai("ACTIVE");
                user.setVaiTros(Set.of(customerRole));
                taiKhoanRepository.save(user);
            }
        };
    }
}
