package com.project.tailsroute.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GridCoordinate {
    private double latitude; // 위도
    private double longitude; // 경도
    private String nx; // 격자 X 좌표
    private String ny; // 격자 Y 좌표

    // 위도와 경도를 격자 좌표로 변환하는 메서드
    public static GridCoordinate fromLatLng(double lat, double lng) {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도 1(degree)
        double SLAT2 = 60.0; // 투영 위도 2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X 좌표(GRID)
        double YO = 136; // 기준점 Y 좌표(GRID)

        double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lng * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int inx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int iny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        String nx = String.valueOf(inx);
        String ny = String.valueOf(iny);

        return new GridCoordinate(lat, lng, nx, ny);
    }
}