package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ChiTietGioHang;
import com.example.demodatn2.entity.GioHang;
import com.example.demodatn2.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Integer> {
    Optional<ChiTietGioHang> findByGioHangAndBienTheSanPham(GioHang gioHang, BienTheSanPham bienTheSanPham);
}
