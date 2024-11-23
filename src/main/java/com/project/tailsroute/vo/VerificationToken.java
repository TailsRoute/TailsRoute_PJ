package com.project.tailsroute.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {
    private String email; // 이메일
    private String code; // 인증번호
    private LocalDateTime expiryDate; // 만료 시간

    // 만료 여부 확인 메서드
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}