package com.project.tailsroute.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class DirectionsController {

    @Value("${OPENROUTESERVICE_API_KEY}")
    private String openRouteServiceApiKey;

    @Value("${NAVER_API}")
    private String apiKeyId;  // 네이버 API Key ID

    @Value("${NAVER_SECRET}")
    private String apiKey;    // 네이버 API Key

    // 주소를 받아 위도, 경도 정보를 반환하는 메소드
    @GetMapping("/reverse-geocode")
    public ResponseEntity<String> getRegionName(@RequestParam double lat, @RequestParam double lon) {

        // 네이버 Reverse Geocoding API 요청 URL
        String url = String.format(
                "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=%f,%f&output=json&orders=addr",
                lon, lat);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", apiKeyId);
        headers.set("X-NCP-APIGW-API-KEY", apiKey);
        headers.set("Accept", "application/json");

        // RestTemplate을 사용하여 API 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // API 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // 결과 JSON 파싱
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray results = jsonResponse.getJSONArray("results");

            if (results.length() > 0) {
                JSONObject region = results.getJSONObject(0).getJSONObject("region");
                String area1 = region.getJSONObject("area1").getString("name"); // 시/도
                String area2 = region.getJSONObject("area2").getString("name"); // 시/군/구
                String area3 = region.getJSONObject("area3").getString("name"); // 읍/면/동

                String regionName = String.format("%s %s %s", area1, area2, area3);

                // 지역명 반환
                return ResponseEntity.ok(regionName);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지역명을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지역명 조회 실패");
        }
    }
    @GetMapping("/geocode")
    public ResponseEntity<String> getGeocode(@RequestParam String query) {
        // 네이버 지오코딩 API 요청 URL
        String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + query;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ncp-apigw-api-key-id", apiKeyId);
        headers.set("x-ncp-apigw-api-key", apiKey);
        headers.set("Accept", "application/json");

        // RestTemplate을 사용하여 API 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        // 결과 반환
        return ResponseEntity.ok(response.getBody());
    }
    @GetMapping("/get-directions")
    public ResponseEntity<String> getDirections(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng) {
        // OpenRouteService의 foot-walking 경로를 위한 URL 구성
        String url = UriComponentsBuilder.fromHttpUrl("https://api.openrouteservice.org/v2/directions/foot-walking")
                .queryParam("start", startLng + "," + startLat)  // 경도, 위도 순서
                .queryParam("end", endLng + "," + endLat)      // 경도, 위도 순서
                .toUriString();
        // 요청 헤더에 API 키 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", openRouteServiceApiKey);
        // RestTemplate을 사용하여 OpenRouteService API 호출
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        // 결과 반환
        return ResponseEntity.ok(response.getBody());
    }
}