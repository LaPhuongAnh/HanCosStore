package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    List<ChiTietDonHang> findByDonHang(DonHang donHang);

    @Query("SELECT c FROM ChiTietDonHang c " +
           "LEFT JOIN FETCH c.bienTheSanPham b " +
           "LEFT JOIN FETCH b.sanPham s " +
           "LEFT JOIN FETCH s.hinhAnhSanPhams " +
           "WHERE c.donHang = :donHang")
    List<ChiTietDonHang> findByDonHangWithDetails(@Param("donHang") DonHang donHang);

    @Query("SELECT SUM(c.soLuong) FROM ChiTietDonHang c WHERE c.donHang.trangThai IN ('HOAN_THANH', 'COMPLETED', 'DELIVERED')")
    Long sumSoLuongDaBan();
}
