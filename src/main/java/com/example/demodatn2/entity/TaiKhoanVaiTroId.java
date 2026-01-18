package com.example.demodatn2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class TaiKhoanVaiTroId implements Serializable {
    private static final long serialVersionUID = -2489513249295753923L;
    @Column(name = "TaiKhoanId", nullable = false)
    private Integer taiKhoanId;

    @Column(name = "VaiTroId", nullable = false)
    private Integer vaiTroId;


}