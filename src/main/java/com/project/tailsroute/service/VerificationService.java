package com.project.tailsroute.service;

import com.project.tailsroute.repository.VerificationTokenRepository;
import com.project.tailsroute.vo.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    // 인증번호 생성
    public void generateVerificationCode(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 이메일입니다.");
        }

        String normalizedEmail = email.toLowerCase().trim();
        tokenRepository.save(normalizedEmail, code);
        System.out.println("인증번호 저장: Email = " + normalizedEmail + ", Code = " + code);
    }


    // 인증번호 검증
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null || email.trim().isEmpty() || code.trim().isEmpty()) {
            System.out.println("인증번호 검증 실패: 잘못된 입력 값");
            return false;
        }

        String normalizedEmail = email.toLowerCase().trim();
        VerificationToken token = tokenRepository.findByEmail(normalizedEmail);

        if (token == null) {
            System.out.println("인증번호 검증 실패: 저장된 데이터 없음 (" + normalizedEmail + ")");
            return false;
        }

        if (token.isExpired()) {
            System.out.println("인증번호 검증 실패: 만료됨 (" + normalizedEmail + ")");
            tokenRepository.deleteByEmail(normalizedEmail);
            return false;
        }

        if (!token.getCode().equals(code)) {
            System.out.println("인증번호 검증 실패: 코드 불일치 (" + normalizedEmail + ")");
            return false;
        }

        System.out.println("인증번호 검증 성공: " + normalizedEmail);
        tokenRepository.deleteByEmail(normalizedEmail); // 검증 성공 후 삭제
        return true;
    }
}