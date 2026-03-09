package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "TAI_KHOAN")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "TenDangNhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "MatKhauHash", nullable = false, length = 100)
    private String matKhau;

    @Column(name = "HoTen", length = 100)
    private String hoTen;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "TrangThai", nullable = false, length = 20)
    private String trangThai = "ACTIVE";

    @Column(name = "NgayTao")
    private java.time.LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private java.time.LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = java.time.LocalDateTime.now();
        ngayCapNhat = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = java.time.LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "TAI_KHOAN_VAI_TRO",
        joinColumns = @JoinColumn(name = "TaiKhoanId"),
        inverseJoinColumns = @JoinColumn(name = "VaiTroId"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"TaiKhoanId", "VaiTroId"})
    )
    private Set<VaiTro> vaiTros = new HashSet<>();
}
