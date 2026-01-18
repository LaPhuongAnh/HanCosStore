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
public class CartItemDTO {
    private Integer id; // ChiTietGioHang Id
    private Integer bienTheId;
    private Integer sanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private String anh;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}
