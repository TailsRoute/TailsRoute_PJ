$(document).ready(function () {
    // 페이지 로드 시 시 데이터를 서버에서 가져옴
    loadCities();

    // 사이드바 열기
    const sidebar = document.getElementById('sidebar');
    const toggleButton = document.getElementById('toggle-button');
    sidebar.classList.add('open');
    toggleButton.innerHTML = '&#9664;'; // 화살표를 오른쪽으로
});

// 시 데이터 로드
function loadCities() {
    $.ajax({
        url: '/cities', method: 'GET', success: function (cities) {
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
            url: '/counties', method: 'GET', data: {city: selectedCity}, success: function (counties) {
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


// 전역 변수로 선언된 map이 이미 존재할 경우 중복 선언하지 않음
if (typeof map === 'undefined') {
    var map;  // let 대신 var로 전역 변수 선언을 수정하여 재선언 방지
}

if (typeof service === 'undefined') {
    var service;
}

// 마커를 저장할 배열 생성
// 기존에 선언된 markers 배열이 있는지 확인
if (typeof markers === 'undefined') {
    var markers = []; // 재선언 방지
}

// 전역 변수로 마커 클러스터 객체 추가
if (typeof markerCluster === 'undefined') {
    var markerCluster;
}

async function initMap() {
    // Google Maps와 Places 라이브러리를 비동기로 불러옴
    const {Map} = await google.maps.importLibrary("maps");

    // 지도 중심 좌표 설정 (서울)
    let center = new google.maps.LatLng(37.5665, 126.9780);

    // 지도를 초기화합니다.
    map = new Map(document.getElementById("map"), {
        center: center, zoom: 14, mapId: google_mapId,
    });

    // 현재 위치를 가져와 지도 중심 설정
    if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition((position) => {
            const {latitude, longitude} = position.coords;
            const userLocation = new google.maps.LatLng(latitude, longitude);
            const geocoder = new google.maps.Geocoder();
            const latlng = {lat: latitude, lng: longitude};


            geocoder.geocode({location: latlng}, (results, status) => {
                if (status === "OK" && results[0]) {
                    const fullAddress = results[0].formatted_address;
                    // console.log("전체 주소:", fullAddress);

                    // 주소에서 시/도와 시/군/구 추출
                    const addressParts = fullAddress.split(" ");
                    if (addressParts.length >= 3) {
                        const city = `${addressParts[1]}`;
                        const region = `${addressParts[2]}`;
                        // console.log("검색 city:", city);
                        // console.log("검색 region:", region);

                        // 검색 API 호출 및 사이드바 업데이트
                        fetchHospitalsWithFilters("", city, region);
                    } else {
                        console.error("주소에서 필요한 정보를 추출할 수 없습니다.");
                    }
                } else {
                    console.error("주소를 가져올 수 없습니다:", status);
                }
            });

            map.setCenter(userLocation); // 지도의 중심을 현재 위치로 설정
        }, (error) => {
            console.error("현재 위치를 가져올 수 없습니다.", error);
        });
    } else {
        console.warn("Geolocation API를 지원하지 않는 브라우저입니다.");
    }

    // 병원 데이터를 지도에 추가하고 마커 클러스터링 적용
    fetchHospitalsAndUpdateMarkers();

}

function fetchHospitalsAndUpdateMarkers(filterType = "") {
    fetch(`/hospitals?type=${filterType}`)
        .then((response) => response.json())
        .then((data) => {

            // 기존 마커 제거
            markers.forEach(marker => marker.setMap(null));
            markers.length = 0;

            // 클러스터링 초기화
            if (markerCluster) {
                markerCluster.clearMarkers();
            }

            // updateSidebar(data);

            // 새로운 마커 추가
            data.forEach((hospital) => {
                if (hospital.latitude && hospital.longitude) {
                    const marker = new google.maps.Marker({
                        position: {lat: parseFloat(hospital.latitude), lng: parseFloat(hospital.longitude)},
                        map: map,
                        title: hospital.name,
                    });

                    // 마커에 병원 ID를 저장
                    marker.hospitalId = hospital.id;
                    markers.push(marker); // 마커 배열에 추가

                    // 클릭 이벤트 추가
                    const infoWindow = new google.maps.InfoWindow({
                        content: `
                <h3>${hospital.name}</h3>
                <p>주소: ${hospital.roadAddress}</p>
                <p>전화번호: ${hospital.callNumber ? hospital.callNumber : '정보 없음'}</p>
            `
                    });

                    marker.addListener('click', () => {
                        infoWindow.open(map, marker);
                    });
                }
            });

            // 마커가 존재할 때만 클러스터링 적용
            if (markers.length > 0) {
                markerCluster = new MarkerClusterer(map, markers, {
                    imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m',
                });
            }
        })
        .catch((error) => console.error("Error fetching hospital data:", error));
}

// 토글 버튼 이벤트로 필터링 적용
document.querySelector('input[role="switch"]').addEventListener("change", function () {
    const isChecked = this.checked;
    const type = isChecked ? "24시간" : "";

    const filterType = document.querySelector('input[role="switch"]').checked ? "24시간" : "";
    const selectedCity = document.getElementById('city-select').value;
    const selectedRegion = document.getElementById('county-select').value;

    if (!selectedCity || (selectedCity !== "세종특별자치시" && !selectedRegion)) {
        alert("시와 군/구를 모두 선택하세요.");
        return;
    }

    fetchHospitalsWithFilters(filterType, selectedCity, selectedRegion);
    fetchHospitalsAndUpdateMarkers(type);
});


// 병원 필터링 함수
function fetchHospitalsWithFilters(filterType, city, region) {
    fetch(`/hospitals/filter?type=${filterType}&region=${encodeURIComponent(city + " " + region)}`)
        .then(response => response.json())
        .then(hospitals => {
            if (Array.isArray(hospitals) && hospitals.length > 0) {
                updateSidebar(hospitals); // 사이드바 업데이트
            } else {
                updateSidebar([]); // 검색 결과 없음 처리
                // alert("검색된 병원이 없습니다.");
            }
        })
        .catch(error => console.error("Error fetching filtered hospitals:", error));
}

// 사이드바 업데이트
function updateSidebar(hospitals) {
    const sidebar = document.getElementById('sidebar');
    const listContainer = sidebar.querySelector('ul');
    const toggleButton = document.getElementById('toggle-button'); // 토글 버튼 참조

    listContainer.innerHTML = ''; // 기존 리스트 항목 초기화

    if (hospitals.length === 0) {
        // alert("hospitals.length == 0");
        // <div class="text-gray-500">검색 결과가 없습니다.</div>
        const listItem = document.createElement('div');
        listItem.classList.add('text-gray-500', 'p-4');
        listItem.innerHTML = `검색 결과가 없습니다.`;
        listContainer.appendChild(listItem);
    }

    hospitals.forEach((hospital) => {
        const listItem = document.createElement('li');
        listItem.classList.add('border-b', 'p-4');
        listItem.innerHTML = `
        <p class="font-semibold">${hospital.name}</p>
        <p class="text-sm text-gray-600">${hospital.roadAddress || hospital.jibunAddress}</p>
        <p class="text-sm text-gray-600">${hospital.callNumber || '전화번호 정보 없음'}</p>
    `;

        // 클릭 이벤트 추가
        listItem.addEventListener('click', () => {
            const marker = markers.find((m) => m.hospitalId === hospital.id);
            if (marker) {
                const position = marker.getPosition();
                map.setCenter(position); // 지도 중심 이동
                map.setZoom(16); // 줌 레벨 조정
                new google.maps.InfoWindow({
                    content: `
                    <h3>${hospital.name}</h3>
                    <p>주소: ${hospital.roadAddress || '주소 정보 없음'}</p>
                    <p>전화번호: ${hospital.callNumber || '전화번호 정보 없음'}</p>
                `
                }).open(map, marker);
            } else {
                console.error("마커를 찾을 수 없습니다:", hospital.id);
            }
        });

        listContainer.appendChild(listItem);
    });

    // hospitals.forEach((hospital, index) => {
    //     const listItem = document.createElement('li');
    //     listItem.classList.add('border-b', 'p-4'); // TailwindCSS 클래스 추가
    //     listItem.innerHTML = `
    //         <p class="font-semibold">${hospital.name}</p>
    //         <p class="text-sm text-gray-600">${hospital.roadAddress || hospital.jibunAddress}</p>
    //         <p class="text-sm text-gray-600">${hospital.callNumber || '전화번호 정보 없음'}</p>
    //     `;
    //
    //     // 클릭 이벤트 추가
    //     listItem.addEventListener('click', () => {
    //         if (markers[index]) {
    //             console.log("index: " + index);
    //             const position = markers[index].getPosition();
    //             map.setCenter(position); // 지도 중심 이동
    //             map.setZoom(16); // 줌 레벨 조정
    //             new google.maps.InfoWindow({
    //                 content: `
    //                     <h3>${hospital.name}</h3>
    //                     <p>주소: ${hospital.roadAddress || '주소 정보 없음'}</p>
    //                     <p>전화번호: ${hospital.callNumber || '전화번호 정보 없음'}</p>
    //                 `
    //             }).open(map, markers[index]);
    //         }
    //
    //         // 사이드바 접기
    //         sidebar.classList.remove('open');
    //         toggleButton.innerHTML = '&#9654;'; // 화살표를 왼쪽으로
    //     });
    //
    //     listContainer.appendChild(listItem); // 리스트 항목 추가
    // });
}

// 검색 버튼 클릭 이벤트 리스너
document.getElementById('search-button').addEventListener('click', () => {
    const filterType = document.querySelector('input[role="switch"]').checked ? "24시간" : "";
    const selectedCity = document.getElementById('city-select').value;
    const selectedRegion = document.getElementById('county-select').value;
    const toggleButton = document.getElementById('toggle-button');

    // 검색 조건 검증
    if (!selectedCity || (selectedCity !== "세종특별자치시" && !selectedRegion)) {
        alert("시와 군/구를 모두 선택하세요.");
        return;
    }

    // 검색 수행
    geocodeAddress();

    // 병원 데이터를 서버에서 가져오기
    fetchHospitalsWithFilters(filterType, selectedCity, selectedRegion);
});

// 신) 사용자가 시/군/구를 선택하여 주소를 좌표로 변환
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
    // console.log("Geocoding 주소:", address);

    geocoder.geocode({address}, function (results, status) {
        if (status === 'OK') {

            const location = results[0].geometry.location;  // 위치 정보 가져오기

            const lat = location.lat();
            const lng = location.lng();

            if (lat && lng) {
                // console.log("Geocoded location (lat, lng):", lat, lng);
                map.setCenter(location);
                map.setZoom(14);  // 줌 설정

            } else {
                console.error('위도 및 경도 값이 null입니다.');
            }

        } else {
            alert("주소 변환 실패: " + status);
        }
    });
}

// CSS javascript
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const toggleButton = document.getElementById('toggle-button');

    sidebar.classList.toggle('open');

    // 토글 버튼의 방향 변경
    if (sidebar.classList.contains('open')) {
        toggleButton.innerHTML = '&#9664;'; // 화살표를 오른쪽으로
    } else {
        toggleButton.innerHTML = '&#9654;'; // 화살표를 왼쪽으로
    }
}
