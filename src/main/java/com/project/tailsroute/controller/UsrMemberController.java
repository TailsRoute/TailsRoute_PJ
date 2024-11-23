package com.project.tailsroute.controller;

import com.project.tailsroute.service.DogService;
import com.project.tailsroute.service.GpsAlertService;
import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.service.VerificationService;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class UsrMemberController {

    private final Rq rq;

    public UsrMemberController(Rq rq) {
        this.rq = rq;
    }

    @Autowired
    private MemberService memberService;

    @Autowired
    private DogService dogService;

    @Autowired
    private GpsAlertService gpsAlertService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/usr/member/login")
    public String showMain(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }

        model.addAttribute("isLogined", isLogined);


        return "usr/member/login";
    }

    @PostMapping("/usr/member/doLogin")
    @ResponseBody
    public Map<String, Object> doLogin(HttpServletRequest req, @RequestParam("loginId") String loginId, @RequestParam("loginPw") String loginPw) {
        Map<String, Object> response = new HashMap<>();

        if (Ut.isEmptyOrNull(loginId)) {
            response.put("resultCode", "F-1");
            response.put("jsAction", "history.back(); alert('loginId 입력 x');");
            return response;
        }

        if (Ut.isEmptyOrNull(loginPw)) {
            response.put("resultCode", "F-2");
            response.put("jsAction", "history.back(); alert('loginPw 입력 x');");
            return response;
        }

        Member member = memberService.getMemberByLoginId(loginId);
        if (member == null) {
            response.put("resultCode", "F-3");
            response.put("jsAction", String.format("history.back(); alert('%s는(은) 존재하지 않습니다.');", loginId));
            return response;
        }

        if (!member.getLoginPw().equals(loginPw)) {
            response.put("resultCode", "F-4");
            response.put("jsAction", "history.back(); alert('비밀번호가 틀렸습니다.');");
            return response;
        }

        rq.login(member);
        response.put("resultCode", "S-1");
        response.put("jsAction", String.format("location.href = '/usr/home/main'; alert('%s님 환영합니다.');", member.getNickname()));
        return response;
    }


    @RequestMapping("/usr/member/doLogout")
    @ResponseBody
    public String doLogout(HttpServletRequest req) {

        rq.logout();

        return Ut.jsReplace("S-1", Ut.f("로그아웃 되었습니다"), "/usr/home/main");
    }

    @GetMapping("/usr/member/myPage")
    public String showMyPage(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }

        Dog dog = dogService.getDogfile(rq.getLoginedMemberId());

        boolean locationChack;

        if (dog != null) {
            GpsAlert gpsAlert = gpsAlertService.getGpsAlert(dog.getId());
            if (gpsAlert == null) {
                locationChack = false;
            } else {
                locationChack = true;

                String location = gpsAlertService.getPlaceName(gpsAlert.getLatitude(), gpsAlert.getLongitude());
                location = location.substring(5);
                model.addAttribute("location", location);
                model.addAttribute("gpsAlert", gpsAlert);
            }
            model.addAttribute("locationChack", locationChack);
        }

        model.addAttribute("isLogined", isLogined);
        model.addAttribute("dog", dog);

        return "usr/member/myPage";
    }

    @GetMapping("/usr/member/join")
    public String showJoin(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }

        model.addAttribute("isLogined", isLogined);


        return "usr/member/join";
    }

    @PostMapping("/usr/member/doJoin")
    @ResponseBody
    public Map<String, Object> doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname, String cellphoneNum, String email) {
        System.err.println("Login ID: " + loginId);
        System.err.println("Login Password: " + loginPw);

        // 회원가입 처리
        ResultData joinRd = memberService.join(loginId, loginPw, name, nickname, cellphoneNum, email);

        // 실패 응답
        if (joinRd.isFail()) {
            return Map.of(
                    "success", false,
                    "message", joinRd.getMsg()
            );
        }
        return Map.of(
                "success", true,
                "message", "회원가입이 성공적으로 처리되었습니다.",
                "redirectUri", "/usr/home/main2"
        );
    }

    @PostMapping("/usr/member/CheckMail")
    @ResponseBody
    public Map<String, Object> sendMail(@RequestParam("mail") String mail) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 인증번호 생성
            Random random = new Random();
            String key = "";
            for (int i = 0; i < 3; i++) {
                int index = random.nextInt(26) + 65; // A~Z
                key += (char) index;
            }
            int numIndex = random.nextInt(9000) + 1000; // 4자리 숫자
            key += numIndex;

            // 이메일 전송
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mail);
            message.setSubject("인증번호 입력을 위한 메일 전송");
            message.setText("인증 번호 : " + key);

            javaMailSender.send(message);

            // 인증번호 저장
            verificationService.generateVerificationCode(mail, key);

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "메일 전송 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }
    @PostMapping("/usr/member/verifyCode")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestParam String mail,
            @RequestParam String code
    ) {
        Map<String, Object> response = new HashMap<>();
        try {

            // 인증번호 검증 호출
            boolean isValid = verificationService.verifyCode(mail, code);

            if (isValid) {
                response.put("success", true);
                response.put("message", "인증번호가 확인되었습니다.");
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                response.put("success", false);
                response.put("message", "인증번호가 올바르지 않거나 만료되었습니다.");
                return ResponseEntity.badRequest().body(response); // HTTP 400
            }
        } catch (Exception e) {
            // 서버 내부 오류 처리
            System.err.println("서버 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 내부 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response); // HTTP 500
        }
    }

}