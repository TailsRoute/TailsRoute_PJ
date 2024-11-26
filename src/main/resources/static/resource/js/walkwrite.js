let markers = [];
let path = []; // 경로를 저장할 배열
let walks = [];
let isAscending = true; // 정렬 방향을 추적하기 위한 변수
let waypoints = [];  // 경유지를 저장할 배열
let polyline = [];
let map;
window.onload = function() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // 월을 두 자리로 변환
    const day = String(today.getDate()).padStart(2, '0'); // 일을 두 자리로 변환
    const currentDate = `${year}-${month}-${day}`;
    // span 요소에 현재 날짜 설정
    document.getElementById('scheduleDate').textContent = currentDate;
    initMap1();
};
function initMap1() {
    if (typeof naver !== 'undefined') {
        const mapContainer = document.getElementById('walkingTrailMap'); // 지도를 표시할 div
        const latitude = gpsCheck1.latitude || 37.56661;  // 위도 값이 없으면 서울의 위도로 설정
        const longitude = gpsCheck1.longitude || 126.978388;  // 경도 값이 없으면 서울의 경도로 설정
        const initialPosition = new naver.maps.LatLng(latitude, longitude); // 네이버 지도 좌표로 초기화
        // 지도 객체 생성
        map = new naver.maps.Map(mapContainer, {
            center: initialPosition,
            zoom: 14,
        });
        // 지도 클릭 이벤트 등록
        naver.maps.Event.addListener(map, 'click', function (event) {
            const latLng = event.coord;  // event.coord로 LatLng 객체 접근
            if (latLng) {
                addLatLng(latLng, waypoints, markers, map);
            } else {
                console.error('latLng 값이 없습니다.');
            }
        });
        // 경로 지우기 버튼 클릭 이벤트 리스너
        document.getElementById("clearPathButton").addEventListener("click", clearPath);
        // 경로 저장 버튼 클릭 이벤트 리스너
        document.getElementById("savePathButton").addEventListener("click", function () {
            const routeName = document.getElementById("routeName").value;
            const scheduleDate = document.getElementById("scheduleDate").textContent;
            const routedistanceText = document.getElementById("routedistance").textContent; // "2.5 km"
            const routedistance = parseFloat(routedistanceText.match(/[0-9.]+/)[0]);
            // 입력값 검사
            if (!routeName) {
                alert("산책명을 적어주세요.");
                return; // 값을 입력하지 않으면 함수 종료
            }
            if (isNaN(routedistance) || routedistance <= 0) {
                alert("루트를 적어주세요.");
                return; // 유효하지 않으면 함수 종료
            }
            const data = JSON.stringify({ path: waypoints }); // 경로 데이터를 문자열화
            const walkData = {
                memberId: w1memberId,
                routeName: routeName,
                purchaseDate: scheduleDate,
                routePicture: data,
                routedistance: routedistance,
                isLiked: 0
            };
            // 서버로 경로 정보 전송
            fetch('/usr/walk/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(walkData)
            })
                .then(response => {
                    if (response.ok) {
                        return response.text();
                    }
                    throw new Error('Network response was not ok.');
                })
                .then(data => {
                    console.log('성공:', data);
                    alert('일정이 추가되었습니다.');
                    window.location.href = "../walk/page";
                })
                .catch((error) => {
                    console.error('실패:', error.message, error.stack);
                    alert('일정 생성 중 오류가 발생했습니다.');
                });
        });
        document.getElementById("showlist").addEventListener("click", function () {
            const walkingSchedule = document.getElementById("WalkingSchedule");
            const showlistButton = document.getElementById("showlist");
            if (walkingSchedule.style.display === "none" || walkingSchedule.style.display === "") {
                // 요소가 보이지 않으면 보이도록 설정
                walkingSchedule.style.display = "inline-block";
                showlistButton.textContent = "리스트 숨기기";
            } else {
                // 요소가 보이면 숨기기
                walkingSchedule.style.display = "none";
                showlistButton.textContent = "리스트 보기";
            }
        });
    }
}
document.getElementById("goBack").addEventListener("click", function() {
    window.location.href = "../walk/page";
});
let totalDistance = 0;
function addLatLng(location, waypoints, markers, map) {
    const lat = location.lat();
    const lng = location.lng();
    const location1 = new naver.maps.LatLng(lat, lng);
    if (waypoints.length === 0) {
        // 첫 클릭은 시작 지점
        waypoints.push(location1);
        const startMarker = new naver.maps.Marker({
            position: location1,
            map: map,
            title: "시작"
        });
        markers.push(startMarker); // 마커 배열에 추가
        map.setCenter(location1);
    } else {
        // 추가 클릭은 목적지로 설정
        waypoints.push(location1);
        const endMarker = new naver.maps.Marker({
            position: location1,
            map: map,
            title: "목적지"
        });
        markers.push(endMarker);
        // Polyline 그리기 (경로 요청 없이)
        const start = waypoints[waypoints.length - 2]; // 이전 지점
        const end = waypoints[waypoints.length - 1];   // 현재 지점
        const path = [start, end]; // 두 점을 연결하는 경로
        const newPolyline = new naver.maps.Polyline({
            map: map,
            path: path,
            strokeColor: '#FF0000',
            strokeWeight: 5,
            strokeOpacity: 0.8
        });
        polyline.push(newPolyline); // 로컬 배열에 폴리라인 추가
        // 두 지점 간의 거리 계산 (km 단위)
        const distance = calculateDistance(start, end);
        totalDistance += distance; // 이전까지의 총 거리 누적
        document.getElementById("routedistance").innerText = `${totalDistance.toFixed(2)} km`;
    }
}
function calculateDistance(point1, point2) {
    const R = 6371; // 지구 반경(km)
    const dLat = toRadian(point2.lat() - point1.lat());
    const dLng = toRadian(point2.lng() - point1.lng());
    const lat1 = toRadian(point1.lat());
    const lat2 = toRadian(point2.lat());
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c; // km로 변환
    return distance;
}
function toRadian(degree) {
    return degree * (Math.PI / 180);
}
function clearPath() {
    if (waypoints.length > 0) {
        // 마지막 지점 제거
        const removedWaypoint = waypoints.pop(); // 마지막 지점을 배열에서 제거
        // 지도에서 마지막 마커 제거
        const removedMarker = markers.pop(); // 마커 배열에서 마지막 마커 제거
        removedMarker.setMap(null); // 지도에서 마커 제거
        // 총거리 계산: 기존 경로를 제거하고 새로 계산
        totalDistance = 0;  // 거리 초기화
        // 경로 계산 (다시 계산해야 하므로 waypoints의 모든 지점을 따라가며 거리 계산)
        for (let i = 1; i < waypoints.length; i++) {
            const start = waypoints[i - 1];
            const end = waypoints[i];
            // 두 점 사이의 거리 계산 (Haversine formula 사용)
            const distance = calculateDistance(start, end);
            totalDistance += distance;
        }
        // 총거리 표시
        document.getElementById("routedistance").innerText = `${totalDistance.toFixed(2)} km`;
        // 경유지가 1개 이하인 경우 지도에서 경로 제거 및 거리 초기화
        if (waypoints.length > 1) {
            // 폴리라인 제거 및 새로운 경로 그리기
            if (polyline.length > 0) {
                const lastPolyline = polyline.pop(); // 배열에서 마지막 폴리라인 제거
                lastPolyline.setMap(null); // 지도에서 마지막 폴리라인 제거
            }
        } else {
            if (polyline.length > 0) {
                const lastPolyline = polyline.pop(); // 배열에서 마지막 폴리라인 제거
                lastPolyline.setMap(null); // 지도에서 마지막 폴리라인 제거
            }
            // 경유지가 1개 이하인 경우 경로 제거
            document.getElementById("routedistance").innerText = "0 km";
        }
    }
}
function searchLocation() {
    const location = document.getElementById("locationInput").value; // 사용자가 입력한 지역
    if (!location) {
        alert("Please enter a location.");
        return;
    }
    // 서버에 위치 검색 요청
    fetch(`/geocode?query=${encodeURIComponent(location)}`)
        .then(response => response.json())
        .then(data => {
            if (data && data.addresses && data.addresses.length > 0) {
                const lat = data.addresses[0].y; // 위도
                const lng = data.addresses[0].x; // 경도
                // 네이버 지도 컨테이너
                const mapContainer = document.getElementById('walkingTrailMap');
                const latLng1 = new naver.maps.LatLng(lat, lng); // 좌표 객체 생성
                console.log(latLng1);
                // 네이버 지도 객체 생성 (초기 위치)
                const map = new naver.maps.Map(mapContainer, {
                    center: latLng1, // 변환된 좌표로 지도의 중심 설정
                    zoom: 14, // 줌 레벨 설정
                });
                naver.maps.Event.addListener(map, 'click', function (event) {
                    const latLng = event.coord;  // event.coord로 LatLng 객체 접근
                    if (latLng) {
                        console.log('클릭한 위치:', latLng);  // 콘솔에 출력
                        addLatLng(latLng, waypoints, markers, map);
                    } else {
                        console.error('latLng 값이 없습니다.');
                    }
                });
            } else {
                alert('No results found for the location.');
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
            alert('Failed to find location. Please try again.');
        });
}
// 위치 입력 필드의 이벤트 리스너
document.getElementById("locationInput").addEventListener("keypress", function(event) {
    if (event.key === "Enter") {
        searchLocation();
    }
});
document.addEventListener("DOMContentLoaded", function() {
    fetchWalks(w1memberId);
});
function renderWalks(walksData) {
    const tableBody = document.querySelector("#WalkingTable tbody");
    tableBody.innerHTML = ""; // 기존 내용을 지웁니다.
    walksData.forEach(item => {
        let icon = "☆"; // 기본 값 (즐겨찾기 전)
        if (item.isLiked == 0) {
            icon = "☆"; // 즐겨찾기 전 (빈 별)
        } else if (item.isLiked == 1) {
            icon = "★"; // 즐겨찾기 후 (채운 별)
        } else {
            icon = "오류";
        }
        const row = document.createElement("tr");
        row.setAttribute("data-route-name", item.routeName);
        row.setAttribute("data-purchase-date", item.purchaseDate);
        row.setAttribute("data-route-distance", item.routedistance);
        row.innerHTML = `
            <td class="ellipsis">${item.routeName}</td>
            <td>${item.purchaseDate}</td>
            <td>${item.routedistance}Km</td>
            <td class="reaction">${icon}</td> <!-- 별 아이콘 표시 -->
        `;
        row.addEventListener("click", function () {
            const routeName = this.getAttribute("data-route-name");
            const purchaseDate = this.getAttribute("data-purchase-date");
            const routeDistance = parseFloat(this.getAttribute("data-route-distance"));
            document.getElementById("routedistance").innerText = "0 km";
            window.totalDistance = 0;
            markers.forEach(marker => marker.setMap(null)); // 모든 마커를 지도에서 제거
            polyline.forEach(line => line.setMap(null));   // 모든 폴리라인을 지도에서 제거
            markers = []; // 마커 배열 초기화
            polyline = []; // 폴리라인 배열 초기화
            waypoints = []; // 경로 배열 초기화
            // 서버로 데이터 요청
            fetch(`/usr/walk/getRoutePicture?routeName=${routeName}&purchaseDate=${purchaseDate}&routedistance=${routeDistance}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('routePicture 데이터를 가져오는 데 실패했습니다.');
                    }
                    return response.json();
                })
                .then(data => {
                    // path 문자열을 JSON으로 파싱
                    const pathData = JSON.parse(data.path).path; // 경로 배열을 얻음
                    totalDistance = 0;
                    // 각 좌표를 처리하여 경로 그리기
                    pathData.forEach((point, index) => {
                        const location = new naver.maps.LatLng(point.y, point.x);
                        // 첫 번째 점은 시작 지점
                        if (index === 0) {
                            waypoints.push(location);
                            const startMarker = new naver.maps.Marker({
                                position: location,
                                map: map,
                                title: "시작"
                            });
                            markers.push(startMarker);
                            map.setCenter(location);
                        } else {
                            // 그 외의 점은 목적지
                            waypoints.push(location);
                            const endMarker = new naver.maps.Marker({
                                position: location,
                                map: map,
                                title: "목적지"
                            });
                            markers.push(endMarker);
                            // 이전 점과 현재 점을 연결하는 경로를 그립니다
                            const start = waypoints[waypoints.length - 2]; // 이전 지점
                            const end = waypoints[waypoints.length - 1];   // 현재 지점
                            const path = [start, end];
                            const newPolyline = new naver.maps.Polyline({
                                map: map,
                                path: path,
                                strokeColor: '#FF0000', // 경로 색상
                                strokeWeight: 5, // 선 두께
                                strokeOpacity: 0.8 // 선 투명도
                            });
                            polyline.push(newPolyline);
                            // 두 점 간 거리 계산
                            const distance = calculateDistance(start, end);
                            totalDistance += distance;
                            // 전체 거리 업데이트
                            document.getElementById("routedistance").innerText = `${totalDistance.toFixed(2)} km`;
                        }
                    });
                })
                .catch(error => {
                    console.error('Error fetching routePicture:', error);
                });
        });
        // reaction (별 아이콘) 클릭 이벤트 추가
        const reactionCell = row.querySelector(".reaction");
        reactionCell.addEventListener("click", function (event) {
            event.stopPropagation(); // row 클릭 이벤트가 발생하지 않도록 방지
            const routeName = row.getAttribute("data-route-name");
            const purchaseDate = row.getAttribute("data-purchase-date");
            const routeDistance = parseFloat(row.getAttribute("data-route-distance"));
            // 클릭 시 isLiked 값 토글 및 DB 업데이트
            const isLikedNew = item.isLiked === 0 ? 1 : 0; // 0에서 1로, 1에서 0으로 토글
            // 서버에 PUT 요청 보내기
            fetch(`/usr/walk/updateLikeStatus`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    isLiked: isLikedNew,
                    routeName: routeName,
                    purchaseDate: purchaseDate,
                    routedistance: routeDistance
                })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('isLiked 상태 업데이트 실패');
                    }
                    return response.json();
                })
                .then(updatedItem => {
                    // 아이콘 업데이트
                    if (isLikedNew === 1) {
                        reactionCell.innerHTML = "★"; // 채운 별
                        item.isLiked = 1;
                    } else {
                        reactionCell.innerHTML = "☆"; // 빈 별
                        item.isLiked = 0;
                    }
                })
                .catch(error => {
                    console.error('Error updating like status:', error);
                });
        });
        tableBody.appendChild(row);
    });
}
function sortByColumn(walksData, column, isAscending) {
    return walksData.sort((a, b) => {
        if (a.isLiked && !b.isLiked) return -1; // a가 'O'이고 b가 'X'일 때 a를 상단으로
        if (!a.isLiked && b.isLiked) return 1;  // a가 'X'이고 b가 'O'일 때 b를 상단으로
        let valueA = a[column] || '';  // undefined 처리
        let valueB = b[column] || '';
        // 날짜 형식일 경우 비교 방법 변경
        if (column === 'purchaseDate') {
            valueA = valueA ? new Date(valueA) : new Date();
            valueB = valueB ? new Date(valueB) : new Date();
        }
        // 정렬 로직 (오름차순 또는 내림차순)
        if (valueA < valueB) return isAscending ? -1 : 1;
        if (valueA > valueB) return isAscending ? 1 : -1;
        return 0;
    });
}
currentSortColumn = 'purchaseDate'; // 초기 정렬 열
const arrows = {
    routeName: document.getElementById("routeNameArrow"),
    purchaseDate: document.getElementById("purchaseDateArrow"),
    routedistance: document.getElementById("routedistanceArrow"),
};
// 화살표 초기화 함수
function resetArrows() {
    for (const key in arrows) {
        arrows[key].textContent = ''; // 모든 화살표 초기화
        arrows[key].classList.remove('visible');
    }
}
// 초기화 함수
function init() {
    resetArrows(); // 화살표 초기화
    arrows.purchaseDate.textContent = '▼'; // 구매일 화살표 표시
    arrows.purchaseDate.classList.add('visible'); // 화살표 보이기
    const sortedWalks = sortByColumn(walks, currentSortColumn, isAscending); // 정렬
    renderWalks(sortedWalks); // 정렬된 데이터 렌더링
}
init(); // 초기화 호출
// 산책명 정렬
document.getElementById("routeNameHeader").addEventListener("click", () => {
    resetArrows(); // 화살표 초기화
    isAscending = currentSortColumn !== 'routeName' || !isAscending; // 정렬 방향 결정
    currentSortColumn = 'routeName';
    arrows.routeName.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
    arrows.routeName.classList.add('visible'); // 화살표 보이기
    const sortedWalks = sortByColumn(walks, 'routeName', isAscending); // 정렬
    renderWalks(sortedWalks); // 정렬된 데이터 렌더링
});
// 산책날짜 정렬
document.getElementById("purchaseDateHeader").addEventListener("click", () => {
    resetArrows(); // 화살표 초기화
    isAscending = currentSortColumn !== 'purchaseDate' || !isAscending; // 정렬 방향 결정
    currentSortColumn = 'purchaseDate';
    arrows.purchaseDate.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
    arrows.purchaseDate.classList.add('visible'); // 화살표 보이기
    const sortedWalks = sortByColumn(walks, 'purchaseDate', isAscending); // 정렬
    renderWalks(sortedWalks); // 정렬된 데이터 렌더링
});
// 산책거리 정렬
document.getElementById("routedistanceHeader").addEventListener("click", () => {
    resetArrows(); // 화살표 초기화
    isAscending = currentSortColumn !== 'routedistance' || !isAscending; // 정렬 방향 결정
    currentSortColumn = 'routedistance';
    arrows.routedistance.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
    arrows.routedistance.classList.add('visible'); // 화살표 보이기
    const sortedWalks = sortByColumn(walks, 'routedistance', isAscending); // 정렬
    renderWalks(sortedWalks); // 정렬된 데이터 렌더링
});
// 품목 리스트를 가져오는 함수
function fetchWalks(w1memberId) {
    fetch(`/usr/walk/get?memberId=` + w1memberId) // memberId 값을 URL에 추가
        .then(response => {
            if (!response.ok) {
                throw new Error('데이터를 가져오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            walks = data;
            // 기본적으로 reaction이 'O'인 항목을 상단에 정렬
            walks.sort((a, b) => {
                const reactionA = a.isLiked === 0 ? 'X' : 'O';
                const reactionB = b.isLiked === 0 ? 'X' : 'O';
                if (reactionA === 'O' && reactionB === 'X') return -1;
                if (reactionA === 'X' && reactionB === 'O') return 1;
                return 0; // reaction이 동일한 경우에는 다른 기준으로 정렬할 수 있도록 함
            });
            // 기본적으로 purchaseDate 기준으로 오름차순 정렬
            walks = sortByColumn(walks, 'purchaseDate', true);
            renderWalks(walks); // 데이터를 렌더링하는 함수 호출
        })
        .catch(error => {
            console.error('Error fetching essentials:', error);
        });
}