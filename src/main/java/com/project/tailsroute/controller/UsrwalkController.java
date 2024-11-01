package com.project.tailsroute.controller;

import com.project.tailsroute.service.GpsChackService;
import com.project.tailsroute.service.WeatherService;
import com.project.tailsroute.vo.GpsChack;
import com.project.tailsroute.vo.GridCoordinate;
import com.project.tailsroute.vo.Member;
import com.project.tailsroute.vo.Rq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class UsrwalkController {
    private final Rq rq;
    private final GpsChackService gpsChackService;
    private final WeatherService weatherService; // WeatherService 인스턴스 추가

    public UsrwalkController(Rq rq, GpsChackService gpsChackService, WeatherService weatherService) {
        this.rq = rq;
        this.gpsChackService = gpsChackService;
        this.weatherService = weatherService; // 초기화
    }

    @Value("${GOOGLE_MAP_API_KEY}")
    private String googleRouteApiKey;

    @GetMapping("/usr/walk/page")
    public String showWalk(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);

            // GPS 정보 가져오기
            GpsChack gpsCheck = gpsChackService.chack(member.getId());
            model.addAttribute("gpsCheck", gpsCheck); // 가져온 GPS 정보 추가
        } else {
            return "redirect:/usr/member/login";
        }

        model.addAttribute("GOOGLE_ROUTE_API_KEY", googleRouteApiKey);
        model.addAttribute("isLogined", isLogined);

        return "usr/walk/page";
    }

    // 날씨 정보 요청을 처리하는 메소드 추가
    @GetMapping("/usr/walk/getWeather")
    @ResponseBody
    public String getWeather(@RequestParam String date, @RequestParam String hour, @RequestParam String nx, @RequestParam String ny) {
        GridCoordinate gridCoordinate = new GridCoordinate();
        gridCoordinate.setNx(nx); // 요청받은 nx 값 사용
        gridCoordinate.setNy(ny); // 요청받은 ny 값 사용

        // 날씨 정보 가져오기
        String weatherInfo = weatherService.getWeatherInfo(gridCoordinate,date, hour);

        return weatherInfo;
    }
}