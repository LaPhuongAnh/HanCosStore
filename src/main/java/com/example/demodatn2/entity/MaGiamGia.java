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
@Table(name = "MA_GIAM_GIA")
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "Ma", nullable = false, length = 50)
    private String ma;

    @Nationalized
    @Column(name = "Loai", nullable = false, length = 20)
    private String loai;

    @Column(name = "GiaTri", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "GiaTriToiDa", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa;

    @Column(name = "DonToiThieu", precision = 18, scale = 2)
    private BigDecimal donToiThieu;

    @Column(name = "SoLuongToiDa")
    private Integer soLuongToiDa;

    @ColumnDefault("0")
    @Column(name = "SoLuongDaDung", nullable = false)
    private Integer soLuongDaDung;

    @Column(name = "BatDauLuc", nullable = false)
    private Instant batDauLuc;

    @Column(name = "KetThucLuc", nullable = false)
    private Instant ketThucLuc;

    @Nationalized
    @ColumnDefault("N'ACTIVE'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;


}