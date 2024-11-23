package com.project.tailsroute.service;

import com.project.tailsroute.repository.MemberRepository;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public Member getMemberByLoginId(String loginId) {
        return memberRepository.getMemberByLoginId(loginId);
    }

    public Member getMemberById(int id) {
        return memberRepository.getMemberById(id);
    }



    public ResultData<Integer> join(String loginId, String loginPw, String name, String nickname, String cellphoneNum,
                                    String email) {

        Member existsMember = getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다.", loginId));
        }

        existsMember = getMemberByNameAndEmail(name, email);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다.", name, email));
        }

        existsMember = getMemberByNameAndcellphoneNum(name, cellphoneNum);

        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 전화번호(%s)입니다.", name, cellphoneNum));
        }

        loginPw = Ut.sha256(loginPw);

        memberRepository.doJoin(loginId, loginPw, name, nickname, cellphoneNum, email);

        int id = memberRepository.getLastInsertId();

        return ResultData.from("S-1", "회원가입 성공", "생성된 회원 id", id);
    }

    public Member getMemberByNameAndEmail(String name, String email) {
        return memberRepository.getMemberByNameAndEmail(name, email);
    }

    public Member getMemberByNameAndcellphoneNum(String name, String cellphoneNum) {
        return memberRepository.getMemberByNameAndcellphoneNum(name, cellphoneNum);
    }


}