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
@Table(name = "PHUONG_THUC_THANH_TOAN")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "Ma", nullable = false, length = 30)
    private String ma;

    @Nationalized
    @Column(name = "Ten", nullable = false, length = 100)
    private String ten;

    @Nationalized
    @ColumnDefault("N'ACTIVE'")
    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;


}