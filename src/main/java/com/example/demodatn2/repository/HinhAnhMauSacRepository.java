package com.example.demodatn2.repository;

import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HinhAnhMauSacRepository extends JpaRepository<HinhAnhMauSac, Integer> {
    List<HinhAnhMauSac> findBySanPham_Id(Integer idSanPham);
    Optional<HinhAnhMauSac> findBySanPham_IdAndMauSac(Integer idSanPham, String mauSac);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM HinhAnhMauSac h WHERE h.sanPham = ?1")
    void deleteBySanPham(SanPham sanPham);
}
