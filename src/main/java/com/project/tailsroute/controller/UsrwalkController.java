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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UsrwalkController {
    private final Rq rq;
    private final GpsChackService gpsChackService;
    private final WeatherService weatherService; // WeatherService 인스턴스 추가
    private final WalkService walkService;

    @Autowired
    public UsrwalkController(Rq rq, GpsChackService gpsChackService, WeatherService weatherService, WalkService walkService) {
        this.rq = rq;
        this.gpsChackService = gpsChackService;
        this.weatherService = weatherService;
        this.walkService = walkService;
    }

    @Value("${NAVER_API}")
    private String NaverApiKey;

    @GetMapping("/usr/walk/page")
    public String showWalk(Model model) {
        boolean isLogined = rq.isLogined();

        if (isLogined) {
            Member member = rq.getLoginedMember();
            model.addAttribute("member", member);

            // GPS 정보 가져오기
            GpsChack gpsCheck = gpsChackService.chack(member.getId());
            if(gpsCheck != null) {
                model.addAttribute("gpsCheck", gpsCheck);
            } else{
                gpsCheck = new GpsChack();
                gpsCheck.setLatitude(37.5665);
                gpsCheck.setLongitude(126.9780);
                model.addAttribute("gpsCheck", gpsCheck);
            }
        } else {
            return "redirect:/usr/member/login";
        }

        model.addAttribute("NAVER_API", NaverApiKey);
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
            if(gpsCheck != null) {
                model.addAttribute("gpsCheck", gpsCheck);
            } else{
                gpsCheck = new GpsChack();
                gpsCheck.setLatitude(37.5665);
                gpsCheck.setLongitude(126.9780);
                model.addAttribute("gpsCheck", gpsCheck);
            }
        } else {
            return "redirect:/usr/member/login";
        }

        model.addAttribute("NAVER_API", NaverApiKey);
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

    @GetMapping("/usr/walk/getcount")
    @ResponseBody
    public List<Walk> countdate(@RequestParam int year, @RequestParam int memberId) {
        return walkService.countdate(year,memberId);
    }

    @GetMapping("/usr/walk/getRoutePicture")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> findRoutePicture(@RequestParam String routeName, @RequestParam String purchaseDate, @RequestParam Double routedistance) {
        Map<String, Object> result = new HashMap<>();
        result.put("path", walkService.findRoutePicture(routeName, purchaseDate, routedistance));
        // JSON 형식으로 반환
        return ResponseEntity.ok(result);
    }
    @PutMapping("/usr/walk/update")
    public ResponseEntity<String> updatedeleteWalks(@RequestBody Walk walk) {
        walkService.updateWalks(
                walk.getRouteName(),
                walk.getPurchaseDate(),
                walk.getRoutePicture(),
                walk.getRoutedistance(),
                walk.getId()
        );
        return ResponseEntity.ok("{\"message\":\"수정 성공\"}");
    }

    @DeleteMapping("/usr/walk/delete")
    public ResponseEntity<String> deleteWalks(@RequestParam int id) {
        walkService.deleteWalks(id);
        return ResponseEntity.ok("{\"message\":\"삭제 성공\"}"); // 성공 메시지 포함
    }
    @PutMapping("/usr/walk/updateLikeStatus")
    public ResponseEntity<String> updateIsLiked(@RequestBody Walk walk) {
        walkService.updateIsLiked(
                walk.getIsLiked(),
                walk.getRouteName(),
                walk.getPurchaseDate(),
                walk.getRoutedistance()
        );
        return ResponseEntity.ok("{\"message\":\"수정 성공\"}");
    }
    @GetMapping("/usr/walk/list")
    public String showWalkList(Model model) {
        boolean isLogined = rq.isLogined();
        if (!isLogined) {
            return "redirect:/usr/member/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }
        // 로그인된 회원 정보 가져오기
        Member member = rq.getLoginedMember();
        int memberId = member.getId(); // 로그인된 회원의 ID 가져오기
        model.addAttribute("member", member);
        model.addAttribute("isLogined", isLogined);
        // GPS 정보 가져오기 (필요한 경우 추가)
        GpsChack gpsCheck = gpsChackService.chack(memberId);
        model.addAttribute("gpsCheck", gpsCheck);
        // 해당 회원의 경로 데이터 조회
        List<Walk> walks = walkService.findWalksByMemberId(memberId);
        model.addAttribute("walks", walks);
        // Google Maps API 키 추가
        model.addAttribute("NAVER_API", NaverApiKey);
        return "usr/walk/list"; // 리스트 페이지로 이동
    }

    @GetMapping("/usr/walk/modify")
    public String showWalkModify(
            @RequestParam String routeName,
            @RequestParam String purchaseDate,
            @RequestParam double routedistance,
            @RequestParam int id,
            Model model) {
        boolean isLogined = rq.isLogined();

        if (!isLogined) {
            return "redirect:/usr/member/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        Member member = rq.getLoginedMember();
        int memberId = member.getId();

        // GPS 정보 가져오기
        GpsChack gpsCheck = gpsChackService.chack(memberId);

        // 저장된 경로 정보 가져오기
        String routePicture = walkService.findRoutePicture(routeName, purchaseDate, routedistance);
        Walk walk = walkService.findWalk(id);

        if (routePicture == null) {
            return "error/noRoute"; // 경로 정보가 없는 경우 에러 페이지로 이동
        }

        model.addAttribute("member", member);
        model.addAttribute("isLogined", isLogined);
        model.addAttribute("gpsCheck", gpsCheck);

        // 수정할 데이터 전달
        model.addAttribute("routeName", routeName);
        model.addAttribute("purchaseDate", purchaseDate);
        model.addAttribute("routedistance", routedistance);
        model.addAttribute("routePicture", routePicture);
        model.addAttribute("walkId", walk.getId()); // walkId 추가
        model.addAttribute("NAVER_API", NaverApiKey);

        return "usr/walk/modify"; // 수정 페이지로 이동
    }
    @GetMapping("/usr/walk/favorites")
    @ResponseBody
    public List<Walk> getFavoriteWalks(@RequestParam int memberId) {
        return walkService.findFavoritesByMemberId(memberId);
    }
}