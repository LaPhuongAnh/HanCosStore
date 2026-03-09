package com.example.demodatn2.dto;
import com.example.demodatn2.entity.DanhMuc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeProductVM {
    private Integer id;
    private String ten;
    private String anhChinh;
    private BigDecimal giaMin;
    private BigDecimal giaMax;
    private List<String> mauSacs;
    private List<String> kichCos;
    private String maDanhMuc;
    private Integer idDanhMuc;
    private DanhMuc danhMuc;

}
