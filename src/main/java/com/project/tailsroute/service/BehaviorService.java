package com.project.tailsroute.service;

import com.project.tailsroute.util.MultipartFileResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class BehaviorService {

    private final RestTemplate restTemplate;

    public BehaviorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeVideo(MultipartFile file) throws IOException {
        String flaskUrl = "http://localhost:5000/analyze"; // Flask 서버 URL

        // 요청 데이터 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartFileResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Flask 서버로 요청 전송
        ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody(); // 성공 시 결과 반환
        } else {
            throw new RuntimeException("Flask 서버 오류: " + response.getStatusCode());
        }
    }
}
