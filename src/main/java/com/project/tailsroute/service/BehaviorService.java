package com.project.tailsroute.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class BehaviorService {

    @Value("${file.upload-dir}") // 파일 저장 디렉토리
    private String uploadDir;

    private final RestTemplate restTemplate;
    private final String flaskUrl = "http://localhost:5000/analyze"; // Flask 서버 URL

    public BehaviorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> analyzeVideo(MultipartFile file) throws IOException {
        // 고유 파일 이름 생성
        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, uniqueFileName);

        // 파일 저장
        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath);

        // 요청 데이터 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath.toFile())); // 저장된 파일 전달

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Flask 서버로 요청 전송
        ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> result = response.getBody();

            // 결과에 파일 경로 추가 (Spring에서 서빙할 수 있도록)
            if (result != null) {
                result.put("video_path", "/uploaded_videos/" + uniqueFileName);
            }
            return result;
        } else {
            throw new RuntimeException("Flask 서버 오류: " + response.getStatusCode());
        }
    }
}
