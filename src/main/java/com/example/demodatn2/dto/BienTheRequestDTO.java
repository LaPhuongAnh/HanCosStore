package com.example.demodatn2.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BienTheRequestDTO {
    private Integer id;
    private String maSKU;
    private String mauSac;
    private String kichCo;
    private BigDecimal gia;
    private BigDecimal giaGoc;
    private Integer soLuongTon;
    private Integer khoiLuongGram;
}
