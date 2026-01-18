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
@Table(name = "VAN_CHUYEN")
public class VanChuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DonHangId", nullable = false)
    private DonHang donHang;

    @Nationalized
    @Column(name = "DonViVanChuyen", nullable = false, length = 100)
    private String donViVanChuyen;

    @Nationalized
    @Column(name = "MaVanDon", length = 100)
    private String maVanDon;

    @Nationalized
    @ColumnDefault("N'CREATED'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "NgayGui")
    private Instant ngayGui;

    @Column(name = "NgayGiao")
    private Instant ngayGiao;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;


}