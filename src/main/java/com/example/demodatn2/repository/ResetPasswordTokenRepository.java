package com.example.demodatn2.repository;

import com.example.demodatn2.entity.ResetPasswordToken;
import com.example.demodatn2.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
    Optional<ResetPasswordToken> findByToken(String token);

    @Modifying
    @Query("UPDATE ResetPasswordToken t SET t.used = true, t.usedAt = :usedAt WHERE t.taiKhoan = :taiKhoan AND t.used = false")
    int markAllUnusedAsUsed(@Param("taiKhoan") TaiKhoan taiKhoan, @Param("usedAt") LocalDateTime usedAt);
}
