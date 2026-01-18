package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "LICH_SU_SU_DUNG_MA_GIAM_GIA")
public class LichSuSuDungMaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaiKhoanId")
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonHangId", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaGiamGiaId", nullable = false)
    private MaGiamGia maGiamGia;

    @ColumnDefault("sysdatetime()")
    @Column(name = "ThoiGianSuDung", nullable = false)
    private Instant thoiGianSuDung;


}