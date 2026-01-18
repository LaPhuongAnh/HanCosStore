package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "GIAO_DICH_TON_KHO")
public class GiaoDichTonKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BienTheSanPhamId", nullable = false)
    private BienTheSanPham bienTheSanPham;

    @Nationalized
    @Column(name = "Loai", nullable = false, length = 20)
    private String loai;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Nationalized
    @Column(name = "ThamChieuLoai", length = 30)
    private String thamChieuLoai;

    @Column(name = "ThamChieuId")
    private Integer thamChieuId;

    @Nationalized
    @Column(name = "GhiChu", length = 300)
    private String ghiChu;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;


}