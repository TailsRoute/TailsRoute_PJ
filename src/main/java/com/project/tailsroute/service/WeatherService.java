package com.project.tailsroute.service;

import com.project.tailsroute.vo.GridCoordinate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${Meteorological_Administration_API_KEY}")
    private String meteorological;

    public String getWeatherInfo(GridCoordinate gridCoordinate, String baseDate, String baseTime) {
        String baseUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"; // 기상청 API URL
        String apiKey = meteorological; // API 키
        String nx = String.valueOf(gridCoordinate.getNx());
        String ny = String.valueOf(gridCoordinate.getNy());

        // 요청 URL 포맷 설정
        String requestUrl = String.format("%s?dataType=JSON&serviceKey=%s&numOfRows=10&pageNo=1&base_date=%s&base_time=%s&nx=%s&ny=%s",
                baseUrl, apiKey, baseDate, baseTime, nx, ny);

        // API 호출 및 결과 반환
        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            return response; // 날씨 정보 요청 결과 반환
        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 스택 트레이스 출력
            return null; // 오류 발생 시 null 반환
        }
    }
}