package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    List<ChiTietDonHang> findByDonHang(DonHang donHang);

    @Query("SELECT SUM(c.soLuong) FROM ChiTietDonHang c WHERE c.donHang.trangThai = 'DELIVERED'")
    Long sumSoLuongDaBan();
}
