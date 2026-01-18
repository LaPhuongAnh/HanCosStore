package com.example.demodatn2.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private String soDienThoai;
}
