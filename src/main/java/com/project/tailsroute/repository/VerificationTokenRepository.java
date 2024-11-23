package com.project.tailsroute.repository;

import com.project.tailsroute.vo.VerificationToken;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class VerificationTokenRepository {

    private final Map<String, VerificationToken> tokens = new ConcurrentHashMap<>();

    public void save(String email, String code) {
        String normalizedEmail = email.toLowerCase().trim();

        tokens.put(normalizedEmail, new VerificationToken(email, code, LocalDateTime.now().plusMinutes(5)));
        if (tokens.containsKey(normalizedEmail)) {
            System.out.println("인증번호 저장 성공: " + normalizedEmail + " -> " + code);
        } else {
            System.out.println("인증번호 저장 실패: " + normalizedEmail);
        }
        debugTokens(); // 저장된 데이터 확인
    }

    // 인증번호 조회
    public VerificationToken findByEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        VerificationToken token = tokens.get(normalizedEmail);
        if (token == null) {
            System.out.println("인증번호 조회 실패: 저장된 데이터 없음 (" + normalizedEmail + ")");
        } else {
            System.out.println("인증번호 조회 성공: " + normalizedEmail + " -> " + token.getCode());
        }
        return token;
    }


    // 인증번호 삭제
    public void deleteByEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        tokens.remove(normalizedEmail);
        debugTokens();
    }

    // 저장된 데이터 디버깅용
    public void debugTokens() {
        tokens.forEach((key, value) -> {
            System.out.println("Email: " + key + ", Code: " + value.getCode() + ", Expiry: " + value.getExpiryDate());
        });
    }
}