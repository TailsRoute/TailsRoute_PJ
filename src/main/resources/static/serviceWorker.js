// serviceWorker.js

// 서비스 워커 설치 이벤트
self.addEventListener('install', function(event) {
    console.log('Service Worker 설치됨');
});

// 서비스 워커 활성화 이벤트
self.addEventListener('activate', function(event) {
    console.log('Service Worker 활성화됨');
});

// 푸시 알림 이벤트 처리
self.addEventListener('push', function(event) {
    let pushData = {};
    let missingId = null;

    // 푸시 메시지 데이터가 있을 경우 처리
    if (event.data) {
        try {
            // JSON 형식 데이터 처리
            pushData = event.data.json();
        } catch (e) {
            console.warn('푸시 메시지 데이터가 JSON이 아닙니다. 문자열로 처리합니다.');
            const messageData = event.data.text(); // 문자열 데이터 처리
            // 문자열에서 메시지와 숫자를 분리

            const messageParts = messageData.split('+');
            pushData.body = messageParts[0]; // 메시지 부분
            missingId = messageParts[1] ? parseInt(messageParts[1]) : null; // 숫자 부분 (missingId)
        }
    }

    // 알림을 보내는 부분
    const title = pushData.title || '알림';
    const options = {
        body: pushData.body || '푸시 알림입니다.',
        icon: pushData.icon || '/uploads/photo/diary1.png',
        badge: pushData.badge || '/uploads/photo/diary2.png',
        data: missingId || null // 추가 데이터(예: missingId)
    };

    // 알림 표시
    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

// 알림 클릭 이벤트 처리
self.addEventListener('notificationclick', function(event) {
    event.notification.close(); // 클릭 시 알림 닫기

    // missingId가 있을 경우 URL에 포함하여 열기
    if (event.notification.data) {
        const missingId = event.notification.data;
        event.waitUntil(
            clients.openWindow(`http://localhost:8081/usr/missing/detail?missingId=${missingId}`)
        );
    }
});


// 알림 클릭 이벤트 처리
self.addEventListener('notificationclick', function(event) {
    event.notification.close(); // 클릭 시 알림 닫기

    event.waitUntil(
        clients.openWindow(`http://localhost:8081/usr/missing/detail?missingId=`+missingId)
    );
});
