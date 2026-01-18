package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanDTO {
    private Integer id;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String trangThai;
    private java.time.LocalDateTime ngayTao;
    private List<Integer> vaiTroIds;
}
