package com.project.tailsroute.service;

import com.fazecast.jSerialComm.SerialPort;
import com.project.tailsroute.repository.GpsAlertRepository;
import com.project.tailsroute.vo.GpsAlert;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class GpsAlertService {

    // Twilio 계정의 SID와 인증 토큰을 저장

    @Value("${SMS_SID}")
    private String ACCOUNT_SID; // Twilio 계정 SID

    @Value("${SMS_TOKEN}")
    private String AUTH_TOKEN; // Twilio 인증 토큰

    private static final double EARTH_RADIUS = 6371.0; // 지구 반지름 (km 단위)

    @Autowired
    GpsAlertRepository gpsAlertRepository;

    // 두 GPS 좌표를 받아 거리 계산 후 SMS 전송 여부 결정
    public void checkDistanceAndSendSms(double lat1, double lon1, GpsAlert gpsAlert) {

        double lat2 = gpsAlert.getLatitude();
        double lon2 = gpsAlert.getLongitude();
        String dogName = gpsAlert.getExtra__dogName();
        int chack = gpsAlert.getChack();

        double distance = calculateDistance(lat1, lon1, lat2, lon2); // 거리 계산

        if (distance > 1.0 && chack == 1) { // 1km 이상 떨어져 있을 때
            sendSms("아이고! "+dogName+"(이)가 정해진 장소를 떠났네요. 위치를 확인해 주세요!", gpsAlert); // SMS 전송
            chack = 0;
        } else { // 다시 구역 안으로 들어왔을때
            chack = 1;
        }
    }

    // 두 지점의 위도와 경도를 받아 거리 계산
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1); // 위도 차이 (라디안 단위로 변환)
        double lonDistance = Math.toRadians(lon2 - lon1); // 경도 차이 (라디안 단위로 변환)

        // 하버사인 공식을 사용하여 두 지점 간의 거리 계산
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); // 구면 삼각법을 이용한 거리 계산
        return EARTH_RADIUS * c; // 계산된 거리를 km 단위로 반환
    }

    // SMS를 전송하는 메서드
    private void sendSms(String messageContent, GpsAlert gpsAlert) {
        // Twilio API 초기화 (계정 SID와 인증 토큰 사용)
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String userCellphoneNum = gpsAlert.getExtra__cellphoneNum();

        // SMS 메시지를 생성하고 전송
        Message message = Message.creator(

                // new PhoneNumber("+1 " + userCellphoneNum), // SMS를 받을 전화번호 (유료결제 해야지 일반번호에 보낼 수 있어서 가상 번호로만 확인 가능)
                new PhoneNumber("+1 8777804236"),
                new PhoneNumber("+1 6305997479"), // Twilio에서 구매한 전화번호
                messageContent // 전송할 메시지 내용
        ).create(); // 메시지를 생성하고 전송

        // 전송된 메시지의 SID를 출력 (디버깅 용도)
        System.err.println("문자 전송됨: " + message.getSid());
    }

    // 프로젝트 시작 시 GPS 데이터 수신을 시작하는 메서드 (한번만 실행됨)
    @PostConstruct
    public void startGpsDataListener() {

        List<GpsAlert> GpsAlerts = gpsAlertRepository.All();

        System.err.println("GPS 갯수 : " +GpsAlerts.size());
        for (GpsAlert gpsAlert : GpsAlerts) {
            System.err.println("GPS들 : " +gpsAlert);
        }

        /*
        String comPortName = determinedLocation.getExtra__comPortName(); // GPS 기기 연결 포트
        double lat2 = determinedLocation.getLatitude(); // 설정한 위도
        double lon2 = determinedLocation.getLongitude(); // 설정한 경도
        String dogName = determinedLocation.getExtra__dogName(); // 강아지 이름
        boolean chack = determinedLocation.getChack(); // 범위 벗어났는지 여부
        String userCellphoneNum = determinedLocation.getExtra__cellphoneNum(); // 주인 핸드폰번호
        */

        // GPS 데이터를 읽고 처리하는 메서드 호출
        for (GpsAlert gpsAlert : GpsAlerts) {
            // 각 GpsAlert에 대해 새로운 쓰레드를 생성하여 readGpsData 실행
            new Thread(() -> readGpsData(gpsAlert)).start();
        }
    }

    /**
     * 아두이노 GPS 트래커에서 데이터를 읽고 처리하는 메소드
     * comPortName : 아두이노 COM 포트 이름 (예: "COM7")
     * lat2 : 비교할 고정된 위도
     * lon2 : 비교할 고정된 경도
     */
    public void readGpsData(GpsAlert gpsAlert) {
        // 아두이노와 통신하기 위한 COM 포트 설정
        SerialPort comPort = SerialPort.getCommPort(gpsAlert.getExtra__comPortName()); // 지정된 COM 포트 열기
        comPort.setComPortParameters(9600, 8, 1, 0); // 포트의 통신 설정 (9600bps, 데이터 비트 8, 스톱 비트 1, 패리티 없음)
        comPort.openPort(); // 포트 열기

        System.out.println(gpsAlert.getExtra__comPortName()+" 연결되었습니다."); // 포트 연결 성공 메시지 출력

        // 데이터를 지속적으로 읽는 루프
        while (true) {
            // 포트에서 읽을 수 있는 데이터가 있는지 확인
            if (comPort.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[comPort.bytesAvailable()]; // 가용 바이트만큼 버퍼 생성
                int numRead = comPort.readBytes(readBuffer, readBuffer.length); // 데이터 읽기
                String receivedData = new String(readBuffer, 0, numRead); // 읽은 데이터를 문자열로 변환
                System.out.println("데이터 수신: " + receivedData); // 수신된 데이터 출력

                // 수신된 데이터가 "위도,경도" 형식인지 확인하여 처리
                try {
                    String[] gpsData = receivedData.trim().split(","); // 데이터가 쉼표로 구분되어 있는지 확인
                    if (gpsData.length == 2) { // GPS 데이터가 유효한 형식인지 확인
                        double latitude = Double.parseDouble(gpsData[0].trim()); // 첫 번째 값: 위도
                        double longitude = Double.parseDouble(gpsData[1].trim()); // 두 번째 값: 경도

                        // 거리 계산 후 기준 거리 이상일 경우 SMS 전송
                        checkDistanceAndSendSms(latitude, longitude, gpsAlert);
                    } else {
                        System.out.println("유효하지 않은 GPS 데이터 형식입니다."); // GPS 데이터 형식이 잘못된 경우 출력
                    }
                } catch (NumberFormatException e) { // 위도, 경도 파싱 중 오류 발생 시 처리
                    System.out.println("GPS 데이터 파싱 실패: " + e.getMessage()); // 파싱 실패 메시지 출력
                }
            }

            // 1초 대기 후 다시 데이터 읽기 시도
            try {
                Thread.sleep(5000); // 5초 동안 대기
            } catch (InterruptedException e) { // 인터럽트 발생 시 오류 처리
                e.printStackTrace(); // 스택 트레이스 출력
            }
        }
    }

    public void saveLocation(int dogId, double latitude, double longitude) {
        // System.err.println("서비스 : " + latitude + ", " + longitude);
        gpsAlertRepository.saveLocation(dogId, latitude, longitude);
    }

    public GpsAlert getGpsAlert(int dogId) {
        return gpsAlertRepository.getGpsAlert(dogId);
    }

    public void updateLocation(int dogId, double latitude, double longitude) {
        gpsAlertRepository.updateLocation(dogId, latitude, longitude);
    }

    public void deleteLocation(int dogId) {
        gpsAlertRepository.deleteLocation(dogId);
    }
}

