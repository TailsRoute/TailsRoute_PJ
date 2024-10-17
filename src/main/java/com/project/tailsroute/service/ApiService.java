package com.project.tailsroute.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String searchProducts(String query) {
        String apiUrl = "https://openapi.naver.com/v1/search/shop.json?query=" + query + "&display=100";

        // 헤더에 Client ID와 Client Secret 설정
        HttpHeaders headers = new HttpHeaders();

        String clientId = System.getenv("NAVER_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_CLIENT_SECRET");

        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 네이버 API 호출
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            // HTTP 오류 처리
            return "{\"error\":\"" + e.getStatusCode() + "\", \"message\":\"" + e.getResponseBodyAsString() + "\"}";
        } catch (Exception e) {
            return "{\"error\":\"상품 검색에 실패했습니다\"}";
        }
    }
}