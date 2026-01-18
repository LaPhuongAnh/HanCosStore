package com.example.demodatn2.repository;

import com.example.demodatn2.entity.GioHang;
import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByTaiKhoan(TaiKhoan taiKhoan);
    Optional<GioHang> findBySessionId(String sessionId);
}
