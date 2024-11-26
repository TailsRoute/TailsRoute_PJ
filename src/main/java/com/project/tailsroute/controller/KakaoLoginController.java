package com.project.tailsroute.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class KakaoLoginController {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final MemberService memberService;
    private final Rq rq;

    @Autowired
    public KakaoLoginController(MemberService memberService, Rq rq) {
        this.memberService = memberService;
        this.rq = rq;
    }


    /*카카오 로그인 페이지로 리다이렉트*/
    @GetMapping("/usr/member/kakao-login")
    public String redirectToKakaoLogin( ) {

        String kakaoLoginUrl = String.format(
                "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
                kakaoClientId, kakaoRedirectUri
        );
        return "redirect:" + kakaoLoginUrl;
    }

    /*카카오 로그인 콜백 처리*/
    @GetMapping("/auth/kakao/callback")
    @ResponseBody
    public String handleKakaoCallback(@RequestParam String code, HttpServletRequest request) {

        Member loginedMember = rq.getLoginedMember();

        if(loginedMember != null) {
            return Ut.jsReplace(loginedMember.getNickname() + "님 이미 로그인 중입니다!", "/usr/home/main");
        }

        // 기존 로직
        String accessToken = getAccessTokenFromKakao(code);

        if (accessToken == null) {
            return Ut.jsReplace("카카오 로그인 실패!", "/");
        }

        Member member = fetchKakaoUserInfo(accessToken);
        if (member == null) {
            return Ut.jsReplace("사용자 정보 처리 중 오류 발생!", "/");
        }

        if (member.isDelStatus()) {
            rq.logout();
            return Ut.rejoin("F-1", Ut.f("탈퇴한 회원입니다, 복구하시겠습니까?"), "/usr/member/doRejoin?id=" + member.getId(), "/usr/home/main");
        }

        // 로그인 성공: 세션에 사용자 정보 저장
        rq.login(member);
        return Ut.jsReplace(member.getNickname() + "님 환영합니다!", "/usr/home/main");
    }


    /*카카오로부터 액세스 토큰 요청*/
    private String getAccessTokenFromKakao(String code) {
        String tokenRequestUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = String.format(
                "grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                kakaoClientId, kakaoRedirectUri, code
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractAccessToken(response.getBody());
        }
        return null;
    }


    /*JSON 응답에서 액세스 토큰 추출*/
    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /*카카오 사용자 정보 요청 및 처리*/
    private Member fetchKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return processUserInfo(response.getBody());
        }
        return null;
    }


    /*사용자 정보 처리 및 회원 가입/로그인*/
    private Member processUserInfo(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String kakaoId = jsonNode.get("id").asText();
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            JsonNode profile = kakaoAccount.get("profile");

            String email = kakaoAccount.get("email").asText();
            String nickname = profile.get("nickname").asText();

            Member existingMember = memberService.getMemberByLoginId(kakaoId);
            if (existingMember != null) {
                return existingMember;
            }

            String randomPassword = memberService.generateRandomPassword();
            memberService.join(kakaoId, randomPassword, nickname, nickname, "N/A", email, 1);

            return memberService.getMemberByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
