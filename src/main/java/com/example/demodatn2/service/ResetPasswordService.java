package com.example.demodatn2.service;

import com.example.demodatn2.entity.ResetPasswordToken;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.repository.ResetPasswordTokenRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Service xử lý luồng quên mật khẩu: tạo token, gửi mail, đổi mật khẩu mới.
public class ResetPasswordService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final int TOKEN_EXPIRE_MINUTES = 15;

    @Transactional
    public void requestResetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        String normalizedEmail = email.trim();
        Optional<TaiKhoan> optUser = taiKhoanRepository.findByEmailIgnoreCase(normalizedEmail);
        if (optUser.isEmpty()) {
            return;
        }

        TaiKhoan user = optUser.get();
        if (!"ACTIVE".equals(user.getTrangThai())) {
            return;
        }

        // Vô hiệu hóa các token cũ chưa dùng
        resetPasswordTokenRepository.markAllUnusedAsUsed(user, LocalDateTime.now());

        ResetPasswordToken token = new ResetPasswordToken();
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setTaiKhoan(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRE_MINUTES));
        token.setUsed(false);
        resetPasswordTokenRepository.save(token);

        // Gửi email reset mật khẩu
        String resetLink = baseUrl + "/reset-password?token=" + token.getToken();
        sendResetEmail(normalizedEmail, resetLink);
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw new RuntimeException("Token không hợp lệ.");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Mật khẩu mới tối thiểu 6 ký tự.");
        }

        ResetPasswordToken token = resetPasswordTokenRepository.findByToken(tokenValue.trim())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn."));

        // Kiểm tra token đã dùng hoặc hết hạn
        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn.");
        }

        TaiKhoan user = token.getTaiKhoan();
        if (user == null || !"ACTIVE".equals(user.getTrangThai())) {
            throw new RuntimeException("Tài khoản không hợp lệ hoặc đã bị khóa.");
        }

        // Hash mật khẩu mới bằng BCrypt
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setMatKhau(hashed);
        taiKhoanRepository.save(user);

        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        resetPasswordTokenRepository.save(token);
    }

    private void sendResetEmail(String email, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(email);
        message.setSubject("Yêu cầu đặt lại mật khẩu");
        message.setText("Bạn vừa yêu cầu đặt lại mật khẩu. Vui lòng bấm link sau để đặt lại mật khẩu (hết hạn sau 15 phút):\n" + resetLink);
        mailSender.send(message);
    }
}
