$(document).ready(function () {
    $(".message").click(function () {
        $(".message").addClass("hidden");
    });

    $(".message2").click(function () {
        $(".message2").addClass("hidden");
    });

    // 페이지 로드 시 시 데이터를 서버에서 가져옴
    loadCities();
    initMap();
});

// 시 데이터 로드
function loadCities() {
    $.ajax({
        url: '/cities',
        method: 'GET',
        success: function (cities) {
            const citySelect = document.getElementById('city-select');
            cities.forEach(city => {
                const option = document.createElement('option');
                option.value = city;
                option.text = city;
                citySelect.appendChild(option);
            });
        }
    });
}

// 선택한 시에 맞는 군/구 데이터를 로드
function loadCounties() {
    const selectedCity = document.getElementById('city-select').value;
    const countySelect = document.getElementById('county-select');

    if (!selectedCity) {
        countySelect.innerHTML = '<option value="">시/군/구 선택</option>';
        return;  // 시가 선택되지 않았으면 실행하지 않음
    }


    // 세종특별자치시 처리 (군/구 선택 없음)
    if (selectedCity === "세종특별자치시") {
        countySelect.innerHTML = '<option value="">시/군/구 없음</option>';
        countySelect.disabled = true;  // 군/구 선택 비활성화
    } else {
        countySelect.disabled = false;
        $.ajax({
            url: '/counties',
            method: 'GET',
            data: {city: selectedCity},
            success: function (counties) {
                countySelect.innerHTML = '<option value="">시/군/구 선택</option>';  // 기존 군 데이터 초기화

                counties.forEach(county => {
                    const option = document.createElement('option');
                    option.value = county;
                    option.text = county;
                    countySelect.appendChild(option);
                });
            }
        });
    }
}

// 선택된 시/군/구를 좌표로 변환하여 지도 중심 이동
function geocodeAddress() {
    const selectedCity = document.getElementById('city-select').value;
    const selectedCounty = document.getElementById('county-select').value;

    if (!selectedCity || (selectedCity !== "세종특별자치시" && !selectedCounty)) {
        alert("시와 군/구를 모두 선택하세요.");
        return;
    }

    const geocoder = new google.maps.Geocoder();
    const address = selectedCity + (selectedCity !== "세종특별자치시" ? " " + selectedCounty : "");

    // 주소 값을 콘솔에 출력하여 확인
    console.log("Geocoding 주소:", address);

    geocoder.geocode({'address': address}, function (results, status) {
        if (status === 'OK' && results[0]) {
            // map.setCenter(results[0].geometry.location);
            const location = results[0].geometry.location;  // 위치 정보 가져오기

            const lat = location.lat();
            const lng = location.lng();

            if (lat && lng) {
                console.log("Geocoded location (lat, lng):", lat, lng);
                map.setCenter(location);
                map.setZoom(14);  // 줌 설정

                // 동물 병원 검색 (Google Places API 사용)
                findAnimalHospitals(location);
            } else {
                console.error('위도 및 경도 값이 null입니다.');
            }

            // 전체 location 객체를 콘솔에 출력하여 구조 확인
            // console.log("Geocoded location object:", location);

            // 콘솔에 location 값 출력 (위도 및 경도 값 확인)
            // console.log("Geocoded location (lat, lng):", location.lat, location.lng);

            // map.setCenter(location);
            // map.setZoom(14);  // 줌 설정

            // 동물 병원 검색 (Google Places API 사용)
            // findAnimalHospitals(location);
        } else {
            console.error('Geocode 실패: ' + status);
            alert('Geocode 실패: ' + status);
        }
    });
}

function findAnimalHospitals(location) {
    const lat = location.lat();
    const lng = location.lng();

    // const apiKey = 'YOUR_API_KEY';  // 본인의 API 키 입력

    const radius = 5000;  // 검색 반경 (단위: 미터)
    const type = 'veterinary_care';  // 동물 병원 검색

    // 클라이언트에서 직접 호출
    // const url = `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${lat},${lng}&radius=${radius}&type=${type}&key=${apiKey}`;

    // 서버 측에서 Google Places API 호출
    const url = `/api/places?location=${lat},${lng}&radius=${radius}&type=${type}`;

    // Fetch API를 사용하여 HTTP 요청 보내기
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'OK') {
                data.results.forEach(place => {
                    createMarker(place);
                });
            } else {
                console.error('Places API 검색 실패: ', data.status);
            }
        })
        .catch(error => console.error('Places API 요청 실패: ', error));
}

// // Google Places API (New)를 사용한 동물 병원 검색 함수
// function findAnimalHospitals(location) {
//     const lat = location.lat();
//     const lng = location.lng();
//
//     const apiKey = 'YOUR_API_KEY'; // 본인의 API 키 입력
//     const radius = 5000; // 검색 반경 (단위: 미터)
//     const type = 'veterinary_care'; // 동물 병원 검색
//
// // Places API (New) 요청 URL
//     const url = `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${lat},${lng}&radius=${radius}&type=${type}&key=${apiKey}`;
//
// // Fetch API를 사용하여 HTTP 요청 보내기
//     fetch(url)
//         .then(response => response.json())
//         .then(data => {
//             if (data.status === 'OK') {
//                 // 검색 결과를 처리하여 마커 생성
//                 data.results.forEach(place => {
//                     createMarker(place);
//                 });
//             } else {
//                 console.error('Places API 검색 실패: ', data.status);
//             }
//         })
//         .catch(error => console.error('Places API 요청 실패: ', error));
// }

// 검색된 병원 정보를 기반으로 마커를 생성하는 함수
function createMarker(place) {
    const marker = new google.maps.Marker({
        map: map,
        position: {lat: place.geometry.location.lat, lng: place.geometry.location.lng},
        title: place.name
    });

    // 마커 클릭 시 병원 이름을 표시
    google.maps.event.addListener(marker, 'click', function () {
        alert(place.name);
    });
}

// 전역 변수로 선언된 map이 이미 존재할 경우 중복 선언하지 않음
if (typeof map === 'undefined') {
    var map;  // let 대신 var로 전역 변수 선언을 수정하여 재선언 방지
}

if (typeof service === 'undefined') {
    var service;
}

// Google Maps 초기화 함수
function initMap() {
    // 지도 표시할 div 요소 가져오기
    const mapDiv = document.getElementById("map");

    if (mapDiv) {
        // map 객체 초기화
        map = new google.maps.Map(mapDiv, {
            center: {lat: 37.5665, lng: 126.9780},  // 서울 좌표
            zoom: 12  // 확대 수준 설정
        });

    } else {
        console.error("Map div is not found or not initialized correctly.");
    }
}

// // Google Maps 초기화 함수 추가
// function initMap() {
//
//     // 지도를 표시할 #map 요소를 찾음
//     const mapDiv = document.getElementById("map");
//
//     // #map 요소가 존재할 때에만 실행
//     if (mapDiv) {
//         // 지도 초기화: 서울 중심으로 설정
//         const map = new google.maps.Map(mapDiv, {
//             center: {lat: 37.5665, lng: 126.9780},  // 서울 좌표
//             zoom: 12  // 확대 수준 설정
//         });
//
//         // 마커를 찍을 위치 목록 (위도, 경도와 설명을 배열로 설정)
//         const locations = [
//             {lat: 37.5665, lng: 126.9780, title: "서울"},
//             {lat: 37.5700, lng: 126.9820, title: "서울 북쪽"},
//             {lat: 37.5500, lng: 126.9900, title: "서울 남쪽"},
//             {lat: 37.5300, lng: 126.9700, title: "서울 서쪽"},
//             {lat: 37.5800, lng: 126.9850, title: "서울 동쪽"}
//         ];
//
//         // 마커 생성
//         const markers = locations.map(location => {
//             return new google.maps.Marker({
//                 position: {lat: location.lat, lng: location.lng},
//                 title: location.title
//             });
//         });
//
//         // 마커 클러스터링 적용
//         new MarkerClusterer(map, markers, {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
//     }
// }

// DOM이 완전히 로드된 후 initMap 호출
// document.addEventListener('DOMContentLoaded', function () {
//     initMap();
// });