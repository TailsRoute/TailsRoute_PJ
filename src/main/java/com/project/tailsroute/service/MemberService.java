package com.project.tailsroute.service;

import com.project.tailsroute.repository.MemberRepository;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final Random random = new Random();

    public Member getMemberByLoginId(String loginId) {
        return memberRepository.getMemberByLoginId(loginId);
    }

    public Member getMemberById(int id) {
        return memberRepository.getMemberById(id);
    }

    public void memberModify(int loginedMemberId, String name, String nickname, String cellphoneNum, String loginPw) {
        memberRepository.memberModify(loginedMemberId, name, nickname, cellphoneNum, loginPw);
    }

    public void memberDelStatus(int loginedMemberId) {
        memberRepository.memberDelStatus(loginedMemberId);
    }

    public void memberReStatus(int loginedMemberId) {
        memberRepository.memberReStatus(loginedMemberId);
    }

    // 인증코드 생성
    public String generateAuthCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    // 이메일 전송
    public void sendAuthCode(String email, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("인증 코드");
        message.setText("회원님의 인증 코드는 다음과 같습니다 : " + authCode);
        mailSender.send(message);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.getMemberByEmail(email);
    }

    public void sendLoginId(String email, String loginId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("TailsRoute");
        message.setText("회원님의 아이디는 다음과 같습니다 : " + loginId);
        mailSender.send(message);
    }

    public void sendLoginPW(String email, String loginPW) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("TailsRoute");
        message.setText("임시 비밀번호가 발급되었습니다 : " + loginPW);
        mailSender.send(message);
    }

    // 랜덤 비밀번호 생성 메서드
    public String generateRandomPassword() {
        // UUID를 사용해 랜덤 문자열 생성 (하이픈 제거)
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12); // 12자리 문자열 반환
    }

    public void setTemporaryPassword(int id, String loginPW) {
        memberRepository.setTemporaryPassword(id, loginPW);
    }

    public ResultData<Integer> join(String loginId, String loginPw, String name, String nickname, String cellphoneNum,
                                    String email, int socialLoginStatus) {

        Member existsMember = getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다.", loginId));
        }

        existsMember = getMemberByNameAndEmail(email);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이메일(%s)입니다.", email));
        }

        existsMember = getMemberByNameAndcellphoneNum(name, cellphoneNum);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 전화번호(%s)입니다.", name, cellphoneNum));
        }

        loginPw = Ut.sha256(loginPw);

        memberRepository.doJoin(loginId, loginPw, name, nickname, cellphoneNum, email, socialLoginStatus);

        int id = memberRepository.getLastInsertId();

        return ResultData.from("S-1", "회원가입 성공", "생성된 회원 id", id);
    }

    public Member getMemberByNameAndEmail(String email) {
        return memberRepository.getMemberByEmail(email);
    }

    public Member getMemberByNameAndcellphoneNum(String name, String cellphoneNum) {
        return memberRepository.getMemberByNameAndcellphoneNum(name, cellphoneNum);
    }

}