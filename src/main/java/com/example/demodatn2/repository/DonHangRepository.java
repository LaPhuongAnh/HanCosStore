package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByTaiKhoanOrderByNgayDatDesc(TaiKhoan taiKhoan);
    List<DonHang> findAllByOrderByNgayDatDesc();
    List<DonHang> findByTrangThaiOrderByNgayDatDesc(String trangThai);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai = 'COMPLETED'")
    BigDecimal sumTongDoanhThu();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai = 'COMPLETED'")
    Long countDonHangThanhCong();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai = 'COMPLETED' AND d.ngayDat >= ?1")
    BigDecimal sumDoanhThuTuNgay(Instant tuNgay);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai = 'COMPLETED' AND d.ngayDat >= ?1")
    Long countDonHangTuNgay(Instant tuNgay);

    @org.springframework.data.jpa.repository.Query("SELECT CAST(d.ngayDat AS LocalDate) as date, SUM(d.tongTien) as amount " +
            "FROM DonHang d WHERE d.trangThai = 'COMPLETED' " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "GROUP BY CAST(d.ngayDat AS LocalDate) " +
            "ORDER BY CAST(d.ngayDat AS LocalDate) ASC")
    List<Object[]> getDoanhThuTheoNgay(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonHang d WHERE d.trangThai = 'COMPLETED' " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
    BigDecimal sumDoanhThuTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai = 'COMPLETED' " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay)")
    Long countDonHangTrongKhoang(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = 'COMPLETED' " +
            "AND (:tuNgay IS NULL OR d.ngayDat >= :tuNgay) " +
            "AND (:denNgay IS NULL OR d.ngayDat <= :denNgay) " +
            "ORDER BY d.ngayDat DESC")
    List<DonHang> findAllDeliveredInRange(@Param("tuNgay") Instant tuNgay, @Param("denNgay") Instant denNgay);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE " +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DonHang> search(@Param("keyword") String keyword);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DonHang d WHERE d.trangThai = :status AND (" +
            "LOWER(d.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.hoTenNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.soDienThoaiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DonHang> searchWithStatus(@Param("keyword") String keyword, @Param("status") String status);
}
