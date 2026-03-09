package com.example.demodatn2.repository;

import com.example.demodatn2.entity.GiaoDichTonKho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiaoDichTonKhoRepository extends JpaRepository<GiaoDichTonKho, Integer> {
    List<GiaoDichTonKho> findByBienTheSanPham_SanPham_IdAndLoaiOrderByNgayTaoDesc(Integer sanPhamId, String loai);
    List<GiaoDichTonKho> findByBienTheSanPham_IdAndLoaiOrderByNgayTaoDesc(Integer bienTheId, String loai);
}
