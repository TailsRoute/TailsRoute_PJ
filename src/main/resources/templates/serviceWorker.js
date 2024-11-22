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
    let title = '푸시 알림 제목';
    let options = {
        body: '푸시 알림 내용입니다.',
        icon: 'icon.png', // 알림 아이콘 경로
        badge: 'icon.png'  // 배지 아이콘 경로 (선택 사항)
    };

    if (event.data) {
        // 수신한 데이터가 JSON 형식인 경우 처리
        const data = event.data.json();
        title = data.title || title;
        options.body = data.body || options.body;
        options.icon = data.icon || options.icon;
    }

    // 알림 표시
    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

// 알림 클릭 시 이벤트 처리
self.addEventListener('notificationclick', function(event) {
    event.notification.close(); // 클릭 시 알림 닫기
    event.waitUntil(
        clients.openWindow('http://localhost:8081') // 클릭 시 열 URL
    );
});
