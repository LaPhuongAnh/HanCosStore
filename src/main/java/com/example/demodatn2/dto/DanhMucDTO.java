package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucDTO {
    private Integer id;
    private String ma;
    private String ten;
    private Integer danhMucChaId;
    private String tenDanhMucCha;
    private String trangThai;
    private LocalDateTime ngayTao;
    private List<DanhMucDTO> danhMucCon;
}
