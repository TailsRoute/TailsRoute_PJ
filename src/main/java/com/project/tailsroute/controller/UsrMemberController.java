package com.project.tailsroute.controller;

import com.project.tailsroute.service.DogService;
import com.project.tailsroute.service.GpsAlertService;
import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.service.VerificationService;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final Rq rq;

    private final Map<String, String> authCodes = new HashMap<>();

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

    @GetMapping("/usr/member/login")
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
    public String doLogin( @RequestParam("loginId") String loginId, @RequestParam("loginPw") String loginPw) {

        // 로그인 상태 확인
        if(rq.isLogined()){
            return Ut.jsReplace("F-0", "이미 로그인된 상태입니다.", "/usr/home/main");
        }

        if (Ut.isEmptyOrNull(loginId)) {
            return Ut.jsHistoryBack("F-1", "loginId를 입력해주세요.");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-2", "loginPw를 입력해주세요.");
        }

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return Ut.jsHistoryBack("F-3", Ut.f("%s는(은) 존재하지 않습니다.", loginId));
        }

        if (member.getLoginPw().equals(loginPw) == false) {
            return Ut.jsHistoryBack("F-4", Ut.f("비밀번호가 올바르지 않습니다."));
        }

        rq.login(member);

        return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), "/usr/home/main");

    }


    @RequestMapping("/usr/member/doLogout")
    @ResponseBody
    public String doLogout(HttpServletRequest req) {

        rq.logout();

        return Ut.jsReplace("S-1", Ut.f("로그아웃 되었습니다"), "/usr/home/main2");
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

    @PostMapping("/usr/member/modify")
    public String doModify(@RequestParam String name, @RequestParam String nickname, @RequestParam String cellphoneNum, @RequestParam String loginPw) {

        if(loginPw.isBlank()){
            // System.err.println("전 : " + loginPw);
            loginPw = rq.getLoginedMember().getLoginPw();
            // System.err.println("후 : " + loginPw);
        }

        memberService.memberModify(rq.getLoginedMemberId(), name, nickname, cellphoneNum, loginPw);

        return "redirect:/usr/member/myPage";
    }

    @PostMapping("/usr/member/delStatus")
    public String doDelStatus() {

        memberService.memberDelStatus(rq.getLoginedMemberId());

        rq.logout();

        return "redirect:/usr/home/main";
    }

    @GetMapping("/usr/member/doRejoin")
    public String doRejoin(@RequestParam int id) {

        memberService.memberReStatus(id);

        Member member = memberService.getMemberById(id);

        rq.login(member);

        return "redirect:/usr/home/main";
    }

    @GetMapping("/usr/member/find")
    public String showFind(Model model) {

        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }

        model.addAttribute("isLogined", isLogined);

        return "/usr/member/find";
    }

    // 아이디 인증코드 요청
    @PostMapping("usr/member/send-code")
    @ResponseBody
    public String sendCode(@RequestParam String email) {

        Member member = memberService.getMemberByEmail(email);

        if(member == null){
            return "해당 회원은 존재하지 않습니다";
        }else if(member.getSocialLoginStatus() == 1){
            return "아이디 찾기 기능을 사용할 수 없습니다.";
        }

        String authCode = memberService.generateAuthCode();
        authCodes.put(email, authCode);
        memberService.sendAuthCode(email, authCode);
        return "인증코드가 전송되었습니다";
    }

    // 아이디 인증코드 검증
    @PostMapping("usr/member/verify-code")
    @ResponseBody
    public String verifyCode(@RequestParam String email, @RequestParam String code) {
        String savedCode = authCodes.get(email);
        Member member = memberService.getMemberByEmail(email);

        if (savedCode != null && savedCode.equals(code)) {
            authCodes.remove(email);
            memberService.sendLoginId(email, member.getLoginId());
            return "인증성공! 아이디가 메일로 전송되었습니다.";
        } else {
            return "인증실패";
        }
    }

    // 비밀번호 인증코드 요청
    @PostMapping("usr/member/send-loginId")
    @ResponseBody
    public String sendLoginId(@RequestParam String loginId) {
        String authCode = memberService.generateAuthCode();

        Member member = memberService.getMemberByLoginId(loginId);

        if(member == null){
            return "해당 회원은 존재하지 않습니다";
        }else if(member.getSocialLoginStatus() == 1){
            return "비밀번호 찾기 기능을 사용할 수 없습니다.";
        }

        authCodes.put(member.getEmail(), authCode);
        memberService.sendAuthCode(member.getEmail(), authCode);
        return "인증코드가 전송되었습니다";
    }

    // 비밀번호 인증코드 검증
    @PostMapping("usr/member/verify-loginId")
    @ResponseBody
    public String verifyLoginId(@RequestParam String loginId, @RequestParam String passwordCode) {
        Member member = memberService.getMemberByLoginId(loginId);

        String savedCode = authCodes.get(member.getEmail());

        String loginPW = memberService.generateRandomPassword();

        memberService.setTemporaryPassword(member.getId(), loginPW);

        if (savedCode != null && savedCode.equals(passwordCode)) {
            authCodes.remove(member.getEmail());
            memberService.sendLoginPW(member.getEmail(), loginPW);
            return "인증성공! 임시비밀번호가 " + member.getEmail() + "로 전송되었습니다.";
        } else {
            return "인증실패";
        }
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
        ResultData joinRd = memberService.join(loginId, loginPw, name, nickname, cellphoneNum, email, 0);

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
    public ResponseEntity<Map<String, Object>> verifyCode2(
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