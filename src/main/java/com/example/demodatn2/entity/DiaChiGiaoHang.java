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
@Table(name = "DIA_CHI_GIAO_HANG")
public class DiaChiGiaoHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TaiKhoanId", nullable = false)
    private TaiKhoan taiKhoan;

    @Nationalized
    @Column(name = "HoTenNhan", nullable = false, length = 150)
    private String hoTenNhan;

    @Nationalized
    @Column(name = "SoDienThoaiNhan", nullable = false, length = 20)
    private String soDienThoaiNhan;

    @Nationalized
    @Column(name = "TinhThanh", nullable = false, length = 100)
    private String tinhThanh;

    @Nationalized
    @Column(name = "QuanHuyen", nullable = false, length = 100)
    private String quanHuyen;

    @Nationalized
    @Column(name = "PhuongXa", nullable = false, length = 100)
    private String phuongXa;

    @Nationalized
    @Column(name = "DiaChiChiTiet", nullable = false, length = 300)
    private String diaChiChiTiet;

    @ColumnDefault("0")
    @Column(name = "LaMacDinh", nullable = false)
    private Boolean laMacDinh;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;


}