package com.example.demodatn2.repository;

import com.example.demodatn2.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    Optional<MaGiamGia> findByMa(String ma);

    @Query("SELECT m FROM MaGiamGia m WHERE m.ma = :ma AND m.trangThai = 'ACTIVE' AND m.batDauLuc <= CURRENT_TIMESTAMP AND m.ketThucLuc >= CURRENT_TIMESTAMP")
    Optional<MaGiamGia> findValidVoucher(String ma);

    @Query("SELECT m FROM MaGiamGia m WHERE m.trangThai = 'ACTIVE' AND m.batDauLuc <= CURRENT_TIMESTAMP AND m.ketThucLuc >= CURRENT_TIMESTAMP AND (m.soLuongToiDa IS NULL OR m.soLuongDaDung < m.soLuongToiDa)")
    java.util.List<MaGiamGia> findAvailableVouchers();
}
