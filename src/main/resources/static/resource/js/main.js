$(document).ready(function () {
    var $slider = $('.slider_container'); // 슬라이더 컨테이너
    var index = 0; // 현재 인덱스
    var totalItems = $('.misssing_item').length - 2; // 총 이미지 개수

    // 왼쪽 버튼 클릭 시 이벤트
    $('.left_btn').on('click', function() {
        index = (index > 0) ? index - 1 : totalItems - 1; // 인덱스 감소, 맨 처음이면 마지막으로 이동
        updateSlider();
    });

    // 오른쪽 버튼 클릭 시 이벤트
    $('.right_btn').on('click', function() {
        index = (index < totalItems - 1) ? index + 1 : 0; // 인덱스 증가, 맨 끝이면 처음으로 이동
        updateSlider();
    });

    // 슬라이드 업데이트 함수
    function updateSlider() {
        var slideWidth = $('.misssing_item').outerWidth(true); // 각 슬라이드의 너비 (마진 포함)
        var newTransformValue = -index * slideWidth; // 새로운 변환 값 계산
        $slider.css('transform', 'translateX(' + newTransformValue + 'px)'); // 슬라이드 이동
    }

    // 설정 시간마다 오른쪽 버튼 클릭 효과 추가
    setInterval(function() {
        index = (index < totalItems - 1) ? index + 1 : 0; // 인덱스 증가, 맨 끝이면 처음으로 이동
        updateSlider();
    }, 3000);

    let timeout;

    $(document).mousemove(function() {
        // 버튼을 보이게 합니다.
        $('.left_btn').fadeIn();
        $('.right_btn').fadeIn();

        // 이전 타이머가 있다면 클리어합니다.
        clearTimeout(timeout);

        // 설정 시간 후에 버튼을 숨기는 타이머 설정
        timeout = setTimeout(function() {
            $('.left_btn').fadeOut();
            $('.right_btn').fadeOut();
        }, 1000);
    });

    var messageFilled =
        "<svg class='flat_icon' xmlns='http://www.w3.org/2000/svg' width='300px' height='300px' viewBox='0 0 100 100' ><path class='circle' d='M50,2.125c26.441,0,47.875,21.434,47.875,47.875c0,26.441-21.434,47.875-47.875,47.875C17.857,97.875,2.125,76.441,2.125,50C2.125,23.559,23.559,2.125,50,2.125z'/><g class='icon'><path class='back' d='M75.139,28.854h-3.807l-8.125-8.125c-0.381-0.38-0.999-0.382-1.381,0l-8.124,8.125H36.428c-2.79,0-5.05,2.261-5.05,5.048v25.247c0,2.79,2.26,5.051,5.05,5.051h38.711c2.789,0,5.05-2.261,5.05-5.051V33.902C80.188,31.115,77.928,28.854,75.139,28.854z'/><path class='front' d='M63.571,35.8H24.862c-2.789,0-5.05,2.262-5.05,5.049v25.247c0,2.788,2.261,5.049,5.05,5.049h3.806l8.125,8.125c0.382,0.381,1,0.383,1.381,0l8.125-8.125h17.275c2.788,0,5.05-2.261,5.05-5.049V40.85C68.62,38.062,66.361,35.8,63.571,35.8z'/><path class='dots' d='M34.743,50.108c-1.858,0-3.365,1.507-3.365,3.366c0,1.86,1.506,3.367,3.365,3.367c1.86,0,3.367-1.507,3.367-3.367C38.109,51.615,36.603,50.108,34.743,50.108z M44.842,50.108c-1.858,0-3.367,1.507-3.367,3.366c0,1.86,1.507,3.367,3.367,3.367c1.859,0,3.366-1.507,3.366-3.367C48.208,51.615,46.701,50.108,44.842,50.108z M54.94,50.108c-1.857,0-3.365,1.507-3.365,3.366c0,1.86,1.506,3.367,3.365,3.367c1.86,0,3.366-1.507,3.366-3.367C58.307,51.615,56.8,50.108,54.94,50.108z'/></g></svg>";
    $(messageFilled).appendTo("#filled-message");

    var filmFilled =
        "<svg class='flat_icon' xmlns='http://www.w3.org/2000/svg' width='300px' height='300px' viewBox='0 0 100 100' ><path class='circle' fill='' d='M50,2.125c26.441,0,47.875,21.434,47.875,47.875S76.441,97.875,50,97.875C17.857,97.875,2.125,76.441,2.125,50S23.559,2.125,50,2.125z'/><g class='icon'><path class='base' d='M72.021,74.154v-3.552c0-1.177-0.954-2.132-2.131-2.132h-2.842c-1.176,0-2.132,0.955-2.132,2.132v3.552H35.081v-3.552c0-1.177-0.955-2.132-2.131-2.132h-2.843c-1.176,0-2.131,0.955-2.131,2.132v3.552h-2.842V25.846h2.842v3.552c0,1.177,0.955,2.132,2.131,2.132h2.843c1.176,0,2.131-0.955,2.131-2.132v-3.552h29.837v3.552c0,1.177,0.955,2.132,2.132,2.132h2.842c1.178,0,2.131-0.955,2.131-2.132v-3.552h2.843v48.309H72.021z M35.081,36.502c0-1.177-0.955-2.132-2.131-2.132h-2.843c-1.176,0-2.131,0.955-2.131,2.132v1.421c0,1.175,0.955,2.132,2.131,2.132h2.843c1.176,0,2.131-0.957,2.131-2.132V36.502z M35.081,45.026c0-1.176-0.955-2.13-2.131-2.13h-2.843c-1.176,0-2.131,0.953-2.131,2.13v1.42c0,1.177,0.955,2.132,2.131,2.132h2.843c1.176,0,2.131-0.955,2.131-2.132V45.026z M35.081,53.552c0-1.177-0.955-2.132-2.131-2.132h-2.843c-1.176,0-2.131,0.955-2.131,2.132v1.421c0,1.176,0.955,2.13,2.131,2.13h2.843c1.176,0,2.131-0.954,2.131-2.13V53.552z M35.081,62.077c0-1.176-0.955-2.132-2.131-2.132h-2.843c-1.176,0-2.131,0.956-2.131,2.132v1.421c0,1.177,0.955,2.132,2.131,2.132h2.843c1.176,0,2.131-0.955,2.131-2.132V62.077z M72.021,36.502c0-1.177-0.954-2.132-2.131-2.132h-2.842c-1.176,0-2.132,0.955-2.132,2.132v1.421c0,1.175,0.955,2.132,2.132,2.132h2.842c1.177,0,2.131-0.957,2.131-2.132V36.502z M72.021,45.026c0-1.176-0.954-2.13-2.131-2.13h-2.842c-1.176,0-2.132,0.953-2.132,2.13v1.42c0,1.177,0.955,2.132,2.132,2.132h2.842c1.177,0,2.131-0.955,2.131-2.132V45.026z M72.021,53.552c0-1.177-0.954-2.132-2.131-2.132h-2.842c-1.176,0-2.132,0.955-2.132,2.132v1.421c0,1.176,0.955,2.13,2.132,2.13h2.842c1.177,0,2.131-0.954,2.131-2.13V53.552z M72.021,62.077c0-1.176-0.954-2.132-2.131-2.132h-2.842c-1.176,0-2.132,0.956-2.132,2.132v1.421c0,1.177,0.955,2.132,2.132,2.132h2.842c1.177,0,2.131-0.955,2.131-2.132V62.077z'/><path class='bottom' d='M40.054,51.42h19.892c1.177,0,2.132,0.955,2.132,2.132v12.787c0,1.177-0.955,2.132-2.132,2.132H40.054c-1.177,0-2.132-0.955-2.132-2.132V53.552C37.922,52.375,38.876,51.42,40.054,51.42z'/><path class='top' fill='' d='M40.054,31.529h19.892c1.177,0,2.132,0.953,2.132,2.13v12.787c0,1.177-0.955,2.132-2.132,2.132H40.054c-1.177,0-2.132-0.955-2.132-2.132V33.659C37.922,32.482,38.876,31.529,40.054,31.529z'/></g></svg>";
    $(filmFilled).appendTo("#filled-film");

    var magnifyFilled =
        "<svg class='flat_icon' xmlns='http://www.w3.org/2000/svg' width='300px' height='300px' viewBox='0 0 100 100' ><path class='circle' d='M50,2.125c26.441,0,47.875,21.434,47.875,47.875S76.441,97.875,50,97.875C17.857,97.875,2.125,76.441,2.125,50S23.559,2.125,50,2.125z'/><g class='icon'><path class='base' d='M72.215,69.959L60.276,58.021c2.497-3.507,3.97-7.792,3.97-12.424c0-11.847-9.604-21.451-21.452-21.451c-11.848,0-21.451,9.604-21.451,21.451c0,11.847,9.604,21.451,21.451,21.451c4.632,0,8.918-1.473,12.425-3.97l11.938,11.938c1.116,1.115,2.928,1.115,4.045,0l1.013-1.014C73.33,72.887,73.33,71.074,72.215,69.959z'/><path class='glass' d='M43.034,30.103c8.689,0,15.732,7.043,15.732,15.731c0,8.689-7.043,15.732-15.732,15.732c-8.688,0-15.731-7.043-15.731-15.732C27.302,37.146,34.345,30.103,43.034,30.103z'/></g></svg>";
    $(magnifyFilled).appendTo("#filled-magnify");
});

function gpsChackOn() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;

            // 위치 정보를 서버로 전송
            $.ajax({
                url: '/usr/gpsChack/on',
                type: 'POST',
                data: {
                    latitude: latitude,
                    longitude: longitude
                },
                success: function (response) {
                    if (response.success) {
                        $('.gps_popup').fadeOut();
                        alert('위치 정보가 저장되었습니다.');
                    } else {
                        alert('위치 정보를 저장하지 못했습니다.');
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Error:', error);
                    alert('위치 정보 저장 중 오류가 발생했습니다.');
                }
            });
        }, function (error) {
            alert('위치 정보를 가져올 수 없습니다. \n먼저 위치 정보 권한을 켜주세요');
            console.error(error);
        });
    } else {
        alert('이 브라우저는 위치 정보를 지원하지 않습니다.');
    }
}

function gpsChackOff() {
    $.ajax({
        url: '/usr/gpsChack/off',
        type: 'POST',
        success: function (response) {
            if (response.success) {
                $('.gps_popup').fadeOut();
                alert('정보 수신을 거절하였습니다');
            }
        },
        error: function (xhr, status, error) {
            console.error('Error:', error);
            alert('정보 수신 거절 중 오류가 발생했습니다.');
        }
    });
}

function gpsChackUpdate() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;

            // 위치 정보를 서버로 전송
            $.ajax({
                url: '/usr/gpsChack/update',
                type: 'POST',
                data: {
                    latitude: latitude,
                    longitude: longitude
                },
                success: function (response) {
                    if (response.success) {
                        $('.gps_popup2').fadeOut();
                        alert('위치 정보가 저장되었습니다.');
                    } else {
                        alert('위치 정보를 저장하지 못했습니다.');
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Error:', error);
                    alert('위치 정보 저장 중 오류가 발생했습니다.');
                }
            });
        }, function (error) {
            alert('위치 정보를 가져올 수 없습니다. \n먼저 위치 정보 권한을 켜주세요');
            console.error(error);
        });
    } else {
        alert('이 브라우저는 위치 정보를 지원하지 않습니다.');
    }
}

function gpsChackCancel() {
    $('.gps_popup2').fadeOut();
    alert('정보 수신을 거절하였습니다');
}