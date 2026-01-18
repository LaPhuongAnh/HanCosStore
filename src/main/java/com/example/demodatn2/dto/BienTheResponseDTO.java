package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BienTheResponseDTO {
    private Integer id;
    private String maSKU;
    private String mauSac;
    private String kichCo;
    private BigDecimal gia;
    private BigDecimal giaGoc;
    private Integer soLuongTon;
    private Integer khoiLuongGram;
    private String trangThai;
}
