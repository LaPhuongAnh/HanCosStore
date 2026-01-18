package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "TAI_KHOAN_VAI_TRO")
public class TaiKhoanVaiTro {
    @EmbeddedId
    private TaiKhoanVaiTroId id;

    @MapsId("taiKhoanId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TaiKhoanId", nullable = false)
    private TaiKhoan taiKhoan;

    @MapsId("vaiTroId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VaiTroId", nullable = false)
    private VaiTro vaiTro;

    @ColumnDefault("sysdatetime()")
    @Column(name = "NgayTao", nullable = false)
    private Instant ngayTao;


}