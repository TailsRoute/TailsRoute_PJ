package com.project.tailsroute.service;


import com.project.tailsroute.repository.WebPushRepository; // 웹푸시 리포지토리

import com.project.tailsroute.vo.WebPush;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebPushService {

    @Value("${Public_Key}")
    private String PUBLIC_KEY;

    @Value("${Private_Key}")
    private String PRIVATE_KEY;

    @Autowired
    private WebPushRepository webPushRepository; // 리포지토리 주입

    public void saveSubscription(int memberId, Subscription subscription) {
        System.err.println("서비스");

        WebPush webPush = webPushRepository.getMemberId(memberId);

        if(webPush != null) {
            if(webPush.getP256dh() != subscription.keys.p256dh){
                webPushRepository.delete(memberId);
            }else if(webPush.getEndpoint() != subscription.endpoint){
                webPushRepository.delete(memberId);
            }else if(webPush.getAuth() != subscription.keys.auth){
                webPushRepository.delete(memberId);
            }else {
                return;
            }
        }

        webPushRepository.save(memberId, subscription); // 구독 정보 저장
    }

    public void sendPushNotification(Subscription subscription, String message) {
        try {
            PushService pushService = new PushService(PUBLIC_KEY, PRIVATE_KEY);
            Notification notification = new Notification(subscription, message);
            pushService.send(notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebPush getSubscription(int memberId) {
        return webPushRepository.getMemberId(memberId);
    }
}
