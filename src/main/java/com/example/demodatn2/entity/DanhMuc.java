package com.example.demodatn2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "DANH_MUC")
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "Ten", nullable = false, length = 150)
    private String ten;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DanhMucChaId")
    @JsonIgnore
    private DanhMuc danhMucCha;

    @OneToMany(mappedBy = "danhMucCha", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DanhMuc> danhMucCon = new ArrayList<>();

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai = "ACTIVE";

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "danhMuc", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SanPham> sanPhams = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = "ACTIVE";
        }
    }


}