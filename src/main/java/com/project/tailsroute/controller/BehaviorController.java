package com.project.tailsroute.controller;

import com.project.tailsroute.service.BehaviorService;
import com.project.tailsroute.util.MultipartFileResource;
import com.project.tailsroute.vo.GpsChack;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/usr/behaviorAnalysis")
public class BehaviorController {

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    private final Rq rq;
    private final BehaviorService behaviorService;

    public BehaviorController(Rq rq, BehaviorService behaviorService) {
        this.rq = rq;
        this.behaviorService = behaviorService;
    }

    // 업로드 페이지 렌더링
    @GetMapping("/videoAnalysis")
    public String videoAnalysisPage(Model model) {
        boolean isLogined = rq.isLogined();
        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);
        }
        model.addAttribute("isLogined", isLogined);
        model.addAttribute("result", null); // 결과 초기화
        return "usr/behaviorAnalysis/videoAnalysis";
    }

    // 비디오 업로드 및 Flask 서버 호출
    @PostMapping("/videoAnalysis")
    public String analyzeVideo(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Flask 서버로 파일 전송
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            // 파일 내용을 메모리에서 바로 전송
            body.add("file", new MultipartFileResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Flask 서버 요청
            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:5000/analyze", requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("[INFO] Flask 서버 응답: " + response.getBody());
                Map<String, Object> result = response.getBody();
                model.addAttribute("message", result.get("message"));
                model.addAttribute("filename", result.get("processed_video"));
                model.addAttribute("result", result.get("result"));
            } else {
                System.err.println("[ERROR] Flask 서버 응답 오류: " + response.getStatusCode());
                model.addAttribute("error", "Flask 서버로부터 오류 응답을 받았습니다.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "파일 업로드 및 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        return "usr/behaviorAnalysis/videoAnalysis"; // 결과 페이지 반환
    }
}
