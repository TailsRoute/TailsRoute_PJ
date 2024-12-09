package com.project.tailsroute.controller;

import com.project.tailsroute.service.BehaviorService;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/usr/behaviorAnalysis")
public class BehaviorController {

    private final Rq rq;

    @Autowired
    private BehaviorService behaviorService;

    public BehaviorController(Rq rq) {
        this.rq = rq;
    }

    // 업로드 페이지 렌더링
    @GetMapping("/videoAnalysis")
    public String videoAnalysisPage(Model model) {
        boolean isLogined = (rq != null) && rq.isLogined();
        model.addAttribute("isLogined", isLogined);
        System.out.println("isLogined = " + isLogined);
        if (isLogined) {
            Member member = rq.getLoginedMember();
            System.out.println("로그인 멤버 = " + member.getName());
            model.addAttribute("member", member); // 로그인된 멤버 정보 전달

        } else{
            model.addAttribute("member", null); // 로그인되지 않은 경우
        }

        model.addAttribute("videoPath", null);
        model.addAttribute("result", null);
        return "usr/behaviorAnalysis/videoAnalysis";
    }

    @Value("${file.upload-dir}") // application.properties에 정의
    private String uploadDir;

    // 비디오 업로드 및 분석 요청 처리
    @PostMapping("/videoAnalysis")
    public String analyzeVideo(@RequestParam("file") MultipartFile file, Model model) {
        boolean isLogined = (rq != null) && rq.isLogined();
        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);

        }
        try {
            // 서비스 호출
            var analysisResult = behaviorService.analyzeVideo(file);

            model.addAttribute("behavior_percentages", analysisResult.get("behavior_percentages"));
            model.addAttribute("most_common_behavior", analysisResult.get("most_common_behavior"));

            System.err.println(analysisResult.get("behavior_percentages"));
            System.err.println(analysisResult.get("most_common_behavior"));

            // 서비스 결과를 모델에 추가
            model.addAttribute("videoPath", analysisResult.get("video_path"));
            System.err.println(analysisResult.get("video_path"));
            model.addAttribute("isLogined", isLogined);

        } catch (Exception e) {
            model.addAttribute("error", "파일 업로드 및 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        return "usr/behaviorAnalysis/videoAnalysis"; // 결과 페이지 반환
    }
}