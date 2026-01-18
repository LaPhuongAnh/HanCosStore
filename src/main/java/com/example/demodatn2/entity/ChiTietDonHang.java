package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "CHI_TIET_DON_HANG")
public class ChiTietDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonHangId", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BienTheSanPhamId", nullable = false)
    private BienTheSanPham bienTheSanPham;

    @Nationalized
    @Column(name = "TenSanPham", nullable = false, length = 200)
    private String tenSanPham;

    @Nationalized
    @Column(name = "MauSac", nullable = false, length = 50)
    private String mauSac;

    @Nationalized
    @Column(name = "KichCo", nullable = false, length = 20)
    private String kichCo;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ThanhTien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;


}