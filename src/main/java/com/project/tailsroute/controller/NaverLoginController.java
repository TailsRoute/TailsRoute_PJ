package com.project.tailsroute.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Controller
public class NaverLoginController {
    private final Rq rq;

    public NaverLoginController(Rq rq) {
        this.rq = rq;
    }

    @Value("${ClientID}")
    private String clientId;  // 네이버 개발자 센터에서 발급받은 Client ID

    @Value("${ClientSecret}")
    private String clientSecret;  // 네이버 개발자 센터에서 발급받은 Client Secret

    @Autowired
    private MemberService memberService;

    @GetMapping("/usr/member/naver-login")
    public String naverLoginRedirect() {
        String redirectUri = "http://localhost:8081/auth/naver/callback";  // 콜백 URL
        String state = "random_state";  // CSRF 방지를 위한 상태 값 (랜덤 생성 추천)

        // 네이버 로그인 URL 생성
        String naverLoginUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;

        // 네이버 로그인 페이지로 리다이렉트
        return "redirect:" + naverLoginUrl;
    }

    @GetMapping("/auth/naver/callback")
    @ResponseBody
    public String naverLoginCallback(@RequestParam String code, @RequestParam String state, Model model) {
        String redirectUri = "http://localhost:8081/auth/naver/callback";

        Member loginedMember = rq.getLoginedMember();

        if(loginedMember != null) {
            return Ut.jsReplace(loginedMember.getNickname() + "님 이미 로그인 중입니다!", "/usr/home/main");
        }

        // 네이버 액세스 토큰 요청
        String accessToken = getNaverAccessToken(code, redirectUri);

        if (accessToken != null) {
            // 네이버 사용자 정보 요청
            Member member = getNaverUserInfo(accessToken);

            if(member.isDelStatus()){
                rq.logout();
                return Ut.rejoin("F-1", Ut.f("탈퇴한 회원입니다, 복구하시겠습니까?"), "/usr/member/doRejoin?id="+member.getId(), "/usr/home/main");
            }

            rq.login(member); // rq 객체를 통해 세션에 로그인 정보 저장
            model.addAttribute("member", member);
            model.addAttribute("isLogined", true);
            return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), "/usr/home/main");
        } else {
            model.addAttribute("isLogined", false);
            return Ut.jsReplace("F-2", Ut.f("로그인에 실패하였습니다"), "/usr/home/main");
        }
    }

    private String getNaverAccessToken(String code, String redirectUri) {
        // 네이버 액세스 토큰 요청 URL
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        // 토큰 요청 파라미터
        String params = UriComponentsBuilder.fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUri)
                .toUriString();

        // HTTP 요청을 사용하여 액세스 토큰 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(params, HttpMethod.GET, entity, String.class);

        // 응답에서 액세스 토큰 추출 (JSON 파싱)
        String accessToken = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            // JSON 파싱 코드 추가 (예: Jackson 사용)
            accessToken = parseAccessToken(responseBody);
        }
        return accessToken;
    }

    private String parseAccessToken(String responseBody) {
        // JSON 파싱 라이브러리 (예: Jackson) 사용하여 액세스 토큰 추출
        // 예시 (Jackson 사용 시):
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Member getNaverUserInfo(String accessToken) {
        // 네이버 사용자 정보 요청 URL
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        // HTTP 요청을 사용하여 사용자 정보 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

        // 응답에서 사용자 정보 추출 (JSON 파싱)
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            // JSON 파싱 코드 추가 (예: Jackson 사용)
            return parseUserInfo(responseBody);
        }
        return null;
    }

    private Member parseUserInfo(String responseBody) {
        // JSON 파싱 라이브러리 (예: Jackson) 사용하여 사용자 정보 추출
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode responseNode = jsonNode.get("response");

            // 사용자 정보 추출
            String loginId = responseNode.get("id").asText();  // 네이버 회원 고유 ID
            String nickname = responseNode.get("nickname").asText();
            String name = responseNode.get("name").asText();
            String cellphoneNum = responseNode.get("mobile").asText();
            String email = responseNode.get("email").asText();

            // 회원 정보 확인
            Member existingMember = memberService.getMemberByLoginId(loginId);

            if (existingMember != null) {
                return existingMember;
            } else {
                // 새로운 회원인 경우 -> 회원가입 처리
                String loginPw = memberService.generateRandomPassword();  // 랜덤 비밀번호 생성

                memberService.join(loginId, loginPw, name, nickname, cellphoneNum, email, 1);   // 세션에 로그인 정보 저장

                Member newMember = memberService.getMemberByEmail(email);
                return newMember;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}