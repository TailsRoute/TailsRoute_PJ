package com.project.tailsroute.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.martijndwars.webpush.Subscription;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebPush {
    private int id;
    private int memberId;
    private String endpoint;
    private String p256dh;
    private String auth;
    private String createdAt;

    // Subscription 객체로 변환하는 메서드
    public Subscription toSubscription() {
        Subscription.Keys keys = new Subscription.Keys(p256dh, auth);
        return new Subscription(endpoint, keys);
    }
}