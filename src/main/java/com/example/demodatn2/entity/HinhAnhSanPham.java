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
@Table(name = "HINH_ANH_SAN_PHAM")
public class HinhAnhSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SanPhamId", nullable = false)
    private SanPham sanPham;

    @Nationalized
    @Column(name = "DuongDanAnh", nullable = false, length = 500)
    private String duongDanAnh;

    @ColumnDefault("0")
    @Column(name = "LaAnhChinh", nullable = false)
    private Boolean laAnhChinh;

    @ColumnDefault("0")
    @Column(name = "ThuTu", nullable = false)
    private Integer thuTu;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }
}