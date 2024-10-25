document.addEventListener('DOMContentLoaded', function () {
    var calendarEl = document.getElementById('calendar');

    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        events: 'calendar', // API 엔드포인트
        eventClick: function(info) {
            alert('Event: ' + info.event.title);
        },
            eventTimeFormat: { // 24시간제로 시간 형식 설정
                hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
            }

    });

    calendar.render(); // 달력 렌더링
});