package com.project.tailsroute.controller;

import com.project.tailsroute.service.DogService;
import com.project.tailsroute.service.GpsAlertService;
import com.project.tailsroute.service.MemberService;
import com.project.tailsroute.util.Ut;
import com.project.tailsroute.vo.Dog;
import com.project.tailsroute.vo.GpsAlert;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UsrMemberController {

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
            return "소셜 로그인 회원은 아이디 찾기 기능을 사용할 수 없습니다. 로그인 시 사용한 소셜 계정으로 로그인해 주세요.";
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
            return "소셜 로그인 회원은 비밀번호 찾기 기능을 사용할 수 없습니다. 로그인 시 사용한 소셜 계정으로 로그인해 주세요.";
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
}