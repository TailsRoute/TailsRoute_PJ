package com.project.tailsroute.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsrHospitalController {

    // 루트 디렉토리에 있는 .env 파일에서 API 키를 불러옴.
    @Value("${GOOGLE_MAP_API_KEY}")
    private String API_KEY;

    @GetMapping("/usr/hospital/main")
    public String showMain(Model model) {

        model.addAttribute("GOOGLE_MAP_API_KEY", API_KEY);
        model.addAttribute("message", "24시간 병원 message 추가");
        return "usr/map/hospital";
    }
}