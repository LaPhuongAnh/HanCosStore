package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "DON_HANG")
public class DonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "MaDonHang", nullable = false, length = 50)
    private String maDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaiKhoanId")
    private TaiKhoan taiKhoan;

    @Nationalized
    @Column(name = "HoTenNhan", nullable = false, length = 150)
    private String hoTenNhan;

    @Nationalized
    @Column(name = "SoDienThoaiNhan", nullable = false, length = 20)
    private String soDienThoaiNhan;

    @Nationalized
    @Column(name = "EmailNhan", length = 150)
    private String emailNhan;

    @Nationalized
    @Column(name = "DiaChiNhan", nullable = false, length = 500)
    private String diaChiNhan;

    @Nationalized
    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Nationalized
    @Column(name = "PhuongThucThanhToan", length = 50)
    private String phuongThucThanhToan;

    @Nationalized
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Nationalized
    @Column(name = "LyDoHuy", length = 500)
    private String lyDoHuy;

    @ColumnDefault("0")
    @Column(name = "TamTinh", nullable = false, precision = 18, scale = 2)
    private BigDecimal tamTinh = BigDecimal.ZERO;

    @ColumnDefault("0")
    @Column(name = "GiamGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal giamGia = BigDecimal.ZERO;

    @ColumnDefault("0")
    @Column(name = "PhiVanChuyen", nullable = false, precision = 18, scale = 2)
    private BigDecimal phiVanChuyen = BigDecimal.ZERO;

    @ColumnDefault("0")
    @Column(name = "TongTien", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGiamGiaId")
    private MaGiamGia maGiamGia;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayDat", nullable = false)
    private Instant ngayDat;

    @Column(name = "NgayCapNhat")
    private Instant ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        if (ngayDat == null) {
            ngayDat = Instant.now();
        }
        ngayCapNhat = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = Instant.now();
    }
}