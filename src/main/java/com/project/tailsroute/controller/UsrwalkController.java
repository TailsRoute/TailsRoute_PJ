package com.project.tailsroute.controller;

import com.project.tailsroute.service.GpsChackService;
import com.project.tailsroute.service.WalkService;
import com.project.tailsroute.service.WeatherService;
import com.project.tailsroute.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UsrwalkController {
    private final Rq rq;
    private final GpsChackService gpsChackService;
    private final WeatherService weatherService; // WeatherService 인스턴스 추가
    private final WalkService walkService;

    @Autowired
    public UsrwalkController(Rq rq, GpsChackService gpsChackService, WeatherService weatherService,WalkService walkService) {
        this.rq = rq;
        this.gpsChackService = gpsChackService;
        this.weatherService = weatherService;
        this.walkService = walkService;
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
    @GetMapping("/usr/walk/write")
    public String showWalkwrite(Model model) {
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

        return "usr/walk/write";
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
    @PostMapping("/usr/walk/create") // POST 요청을 처리하는 메소드
    public ResponseEntity<String> createWalk(@RequestBody Walk walk) {
        try {
            walkService.addWalk(walk); // WalkService를 통해 데이터베이스에 저장합니다.
            return new ResponseEntity<>("일정이 성공적으로 생성되었습니다.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("일정 생성 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/usr/walk/get")
    @ResponseBody
    public List<Walk> getWalks(@RequestParam int memberId) {
        return walkService.findWalksByMemberId(memberId);
    }
    @DeleteMapping("/usr/walk/delete")
    public ResponseEntity<String> deleteWalks(@RequestParam int id) {
        walkService.deleteWalks(id);
        return ResponseEntity.ok("{\"message\":\"삭제 성공\"}"); // 성공 메시지 포함
    }
    @PutMapping("/usr/walk/update")
    public ResponseEntity<String> updatedeleteWalks(@RequestBody Walk walk) {
        walkService.updateWalks(
                walk.getRouteName(),
                walk.getPurchaseDate(),
                walk.getId()
        );
        return ResponseEntity.ok("{\"message\":\"수정 성공\"}");
    }
}