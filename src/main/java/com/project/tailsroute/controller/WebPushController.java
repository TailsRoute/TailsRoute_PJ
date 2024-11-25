package com.project.tailsroute.controller;

import com.project.tailsroute.service.WebPushService;
import com.project.tailsroute.vo.Rq;


import com.project.tailsroute.vo.WebPush;
import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class WebPushController {
    private final Rq rq;
    public WebPushController(Rq rq) {
        this.rq = rq;
    }

    @Value("${Public_Key}")
    private String PUBLIC_KEY;

    @Autowired
    private WebPushService webPushService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Subscription subscription) {
        System.err.println("컨트롤러 실행됨");
        System.err.println("구독 정보: " + subscription);

        try {
            int memberId = rq.getLoginedMemberId();
            // 구독 정보에서 base64로 인코딩된 키를 사용하여 처리

            webPushService.saveSubscription(memberId, subscription); // 리포지토리에 구독 정보 저장
            return ResponseEntity.ok("Subscribed successfully");
        } catch (Exception e) {
            e.printStackTrace(); // 예외 스택 추적 출력
            System.err.println("오류 메시지: " + e.getMessage()); // 오류 메시지 추가 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Subscription failed");
        }
    }

    @GetMapping("/subscribe")
    public String test1(Model model) {
        model.addAttribute("PUBLIC_KEY", PUBLIC_KEY);
        return "usr/missing/WebPush";
    }

    @PostMapping("/sendPush")
    public ResponseEntity<?> sendPush(@RequestBody Map<String, Object> request) {
        try {
            int memberId = rq.getLoginedMemberId();
            WebPush subscription = webPushService.getSubscription(memberId);

            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Subscription not found");
            }

            // 요청 데이터에서 message와 missingId 가져오기
            String message = (String) request.get("message");

            if (message == null || message.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Message is empty");
            }

            // 푸시 알림 전송
            webPushService.sendPushNotification(subscription.toSubscription(), message);
            return ResponseEntity.ok("Push notification sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send push notification");
        }
    }
}
