package com.example.demodatn2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "CHI_TIET_DOI_TRA")
public class ChiTietDoiTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Nationalized
    @Column(name = "GhiChu", length = 300)
    private String ghiChu;


}