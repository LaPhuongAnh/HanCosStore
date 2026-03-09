package com.example.demodatn2.controller;

import com.example.demodatn2.dto.ForgotPasswordRequestDTO;
import com.example.demodatn2.dto.ResetPasswordRequestDTO;
import com.example.demodatn2.service.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        resetPasswordService.requestResetPassword(request.getEmail());
        // Không tiết lộ email có tồn tại hay không
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, hệ thống đã gửi link đặt lại mật khẩu."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        resetPasswordService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công."));
    }
}
