package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuDTO {
    private BigDecimal tongDoanhThu;
    private Long soDonHang;
    private Long soSanPhamDaBan;
    private BigDecimal doanhThuHomNay;
    private Long soDonHomNay;
}
