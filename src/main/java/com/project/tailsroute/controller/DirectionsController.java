package com.project.tailsroute.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/geocode")
    public ResponseEntity<String> getGeocode(@RequestParam String query) {

        // 네이버 지오코딩 API 요청 URL
        String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + query;

        // HTTP 헤더 설정
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