let markers = [];
let path = []; // 경로를 저장할 배열
let walks = [];
let isAscending = true; // 정렬 방향을 추적하기 위한 변수
let waypoints = [];  // 경유지를 저장할 배열
let polyline = [];
let map;
var RE = 6371.00877; // 지구 반경(km)
var GRID = 5.0; // 격자 간격(km)
var SLAT1 = 30.0; // 투영 위도1(degree)
var SLAT2 = 60.0; // 투영 위도2(degree)
var OLON = 126.0; // 기준점 경도(degree)
var OLAT = 38.0; // 기준점 위도(degree)
var XO = 43; // 기준점 X좌표(GRID)
var YO = 136; // 기준점 Y좌표(GRID)
function dfs_xy_conv(code, v1, v2) {
    var DEGRAD = Math.PI / 180.0;
    var RADDEG = 180.0 / Math.PI;
    var re = RE / GRID;
    var slat1 = SLAT1 * DEGRAD;
    var slat2 = SLAT2 * DEGRAD;
    var olon = OLON * DEGRAD;
    var olat = OLAT * DEGRAD;
    var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
    var ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);
    var rs = {};
    if (code == "toXY") {
        rs['lat'] = v1; // 위도
        rs['lng'] = v2; // 경도
        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        var theta = v2 * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        rs['x'] = Math.floor(ra * Math.sin(theta) + XO + 0.5);
        rs['y'] = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
    } else {
        rs['x'] = v1; // X좌표
        rs['y'] = v2; // Y좌표
        var xn = v1 - XO;
        var yn = ro - v2 + YO;
        ra = Math.sqrt(xn * xn + yn * yn);
        if (sn < 0.0) ra = -ra;
        var alat = Math.pow((re * sf / ra), (1.0 / sn));
        alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;
        if (Math.abs(xn) <= 0.0) {
            theta = 0.0;
        } else {
            if (Math.abs(yn) <= 0.0) {
                theta = Math.PI * 0.5;
                if (xn < 0.0) theta = -theta;
            } else {
                theta = Math.atan2(xn, yn);
            }
        }
        var alon = theta / sn + olon;
        rs['lat'] = alat * RADDEG; // 변환된 위도
        rs['lng'] = alon * RADDEG; // 변환된 경도
    }
    return rs; // 결과 반환
}
// 위경도를 격자 좌표로 변환하는 함수
function convertLatLngToGrid(latitude, longitude) {
    var result = dfs_xy_conv("toXY", latitude, longitude);
    return {
        nx: String(result.x), // nx를 문자열로 변환
        ny: String(result.y)  // ny를 문자열로 변환
    };
}
window.onload = function() {
    initMap();
};
function initMap() {
    if (typeof naver !== 'undefined') {
        const mapContainer = document.getElementById('walkingTrailMap'); // 지도를 표시할 div
        const latitude = gpsCheck.latitude || 37.56661;  // 위도 값이 없으면 서울의 위도로 설정
        const longitude = gpsCheck.longitude || 126.978388;  // 경도 값이 없으면 서울의 경도로 설정
        const initialPosition = new naver.maps.LatLng(latitude, longitude); // 네이버 지도 좌표로 초기화
        var gridCoordinates = convertLatLngToGrid(latitude, longitude);
        getWeatherInfo(gridCoordinates.nx, gridCoordinates.ny,latitude,longitude); // 날씨 정보 요청
        // 지도 객체 생성
        map = new naver.maps.Map(mapContainer, {
            center: initialPosition,
            zoom: 14,
        });
    }
}
function generateGrassMap(year, dailyWalkCounts) {
    const grid = document.querySelector('.grid');
    const firstDay = new Date(year, 0, 1).getDay(); // 연도의 1월 1일의 요일
    const daysInMonth = [31, 28 + (isLeapYear(year) ? 1 : 0), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    grid.innerHTML = ''; // 기존 내용을 초기화
    let currentDayOfWeek = firstDay; // 첫 번째 날짜의 요일
    let weekCounter = 0;
    // 1월부터 12월까지 날짜 그리기
    for (let month = 0; month < 12; month++) {
        for (let day = 1; day <= daysInMonth[month]; day++) {
            // 각 날짜에 맞는 위치를 계산
            if (month === 0 && day === 1) {
                weekCounter = 0; // 첫 주는 0번째 줄
            } else if (currentDayOfWeek === 0) {
                weekCounter++; // 매주 일요일이 지나면 새로운 줄로
            }
            // 날짜에 대한 카운트 값을 찾기 (예: '1월 1일')
            const dateKey = `${month + 1}월 ${day}일`;
            const dailyCount = dailyWalkCounts[dateKey] || 0; // 해당 날짜의 카운트 값 (없으면 0)
            const box = document.createElement('div');
            box.className = 'box';
            box.dataset.date = dateKey+'('+dailyCount+'회)'; // 툴팁 표시 날짜
            // 색상 변경 로직: 카운트 값에 따라 색상 다르게 적용
            if (dailyCount === 0) {
                box.classList.add('count-0');
            } else if (dailyCount === 1) {
                box.classList.add('count-1');
            } else if (dailyCount === 2) {
                box.classList.add('count-2');
            } else if (dailyCount === 3) {
                box.classList.add('count-3');
            } else {
                box.classList.add('count-4-plus');
            }
            box.style.gridColumnStart = weekCounter + 1;
            box.style.gridRowStart = currentDayOfWeek + 1;
            box.addEventListener('click', () => {
                box.classList.toggle('active'); // 활성화/비활성화 전환
            });
            grid.appendChild(box);
            // 다음 날짜로 넘어감
            currentDayOfWeek = (currentDayOfWeek + 1) % 7;
        }
    }
}
// 윤년 계산
function isLeapYear(year) {
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}
document.addEventListener('DOMContentLoaded', () => {
    const year = new Date().getFullYear();
    fetch(`/usr/walk/getcount?year=${year}&memberId=${wmemberId}`)
        .then(response => response.json())
        .then(data => {
            const dailyWalkCounts = {};
            data.forEach(item => {
                const date = new Date(item.date);
                const month = date.getMonth() + 1; // 0부터 시작하므로 1을 더함
                const day = date.getDate();
                const dateKey = `${month}월 ${day}일`;
                dailyWalkCounts[dateKey] = item.extra__count; // 서버에서 받아온 카운트 값을 사용
            });
            generateGrassMap(year, dailyWalkCounts); // 카운트를 기반으로 그리드 생성
        })
        .catch(error => console.error('Error fetching data:', error));
});
document.getElementById("writeWalkButton").addEventListener("click", function() {
    window.location.href = "../walk/write";
});
document.addEventListener("DOMContentLoaded", function() {
    fetchWalks(wmemberId);
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
            <td>
            <button class="modify-btn"
                        data-route-name="${item.routeName}"
                        data-purchase-date="${item.purchaseDate}"
                        data-route-distance="${item.routedistance}"
                        data-route-id="${item.id}">🔧</button></td>
            <td>
            <button class="delete-btn"
                        data-route-name="${item.routeName}"
                        data-purchase-date="${item.purchaseDate}"
                        data-route-distance="${item.routedistance}"
                        data-route-id="${item.id}">🗑</button></td>
        `;
        row.addEventListener("click", function () {
            const routeName = this.getAttribute("data-route-name");
            const purchaseDate = this.getAttribute("data-purchase-date");
            const routeDistance = parseFloat(this.getAttribute("data-route-distance"));
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
                        }
                    });
                })
                .catch(error => {
                    console.error('Error fetching routePicture:', error);
                });
        });
        const modifyButtons = row.querySelectorAll(".modify-btn");
        modifyButtons.forEach(button => {
            button.addEventListener("click", function (event) {
                event.stopPropagation();
                const routeName = this.getAttribute("data-route-name");
                const purchaseDate = this.getAttribute("data-purchase-date");
                const routeDistance = parseFloat(this.getAttribute("data-route-distance"));
                const routeId = this.getAttribute("data-route-id");
                // 비동기 fetch 요청 후, URL로 이동
                fetch(`/usr/walk/getRoutePicture?routeName=${routeName}&purchaseDate=${purchaseDate}&routedistance=${routeDistance}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('routePicture 데이터를 가져오는 데 실패했습니다.');
                        }
                        return response.json();
                    })
                    .then(data => {
                        const pathData = JSON.parse(data.path);  // JSON 파싱 후 pathData에 값 설정
                        console.log(pathData);
                        // pathData를 JSON 문자열로 변환 후 URL에 추가
                        const pathDataStr = encodeURIComponent(JSON.stringify(pathData));
                        // 쿼리 파라미터를 URL에 추가
                        const queryParams = new URLSearchParams({
                            id: routeId,
                            routeName: routeName,
                            purchaseDate: purchaseDate,
                            routedistance: routeDistance
                        }).toString();
                        // URL에 쿼리 파라미터를 추가하여 페이지 이동
                        window.location.href = `../walk/modify?${queryParams}`;
                    })
                    .catch(error => {
                        console.error('Error fetching routePicture:', error);
                    });
            });
        });
        const deleteButtons = row.querySelectorAll(".delete-btn");
        deleteButtons.forEach(button => {
            button.replaceWith(button.cloneNode(true)); // 버튼 복제 및 기존 리스너 제거
        });
        row.querySelectorAll(".delete-btn").forEach(button => {
            button.addEventListener("click", function (event) {
                event.stopPropagation();
                const routeId = this.getAttribute("data-route-id");
                const confirmDelete = confirm("정말로 삭제하시겠습니까?");
                if (confirmDelete) {
                    // 삭제 요청 보내기
                    fetch(`/usr/walk/delete?id=${routeId}`, {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error("삭제 요청이 실패했습니다.");
                            }
                            return response.json();
                        })
                        .then(data => {
                            console.log(data.message); // 서버에서 전달된 성공 메시지 출력
                            alert("삭제가 완료되었습니다.");
                            // 삭제된 항목 제거 (예: 테이블에서 해당 행 삭제)
                            this.closest("tr").remove(); // 버튼의 부모 행 삭제
                        })
                        .catch(error => {
                            console.error("Error:", error);
                            alert("삭제 중 오류가 발생했습니다.");
                        });
                } else {
                    // 사용자가 "아니오"를 선택한 경우
                    alert("삭제가 취소되었습니다.");
                }
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
        row.addEventListener("click", function () {
            // 데이터 속성에서 값을 가져옵니다.
            const routeName = this.getAttribute("data-route-name");
            const purchaseDate = this.getAttribute("data-purchase-date");
            const routeDistance = parseFloat(this.getAttribute("data-route-distance"));
            // 서버로 데이터 요청 (routePicture 관련 부분은 그대로 유지)
            fetch(`/usr/walk/getRoutePicture?routeName=${routeName}&purchaseDate=${purchaseDate}&routedistance=${routeDistance}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('routePicture 데이터를 가져오는 데 실패했습니다.');
                    }
                    return response.json();
                })
                .then(data => {
                    const mapContainer = document.getElementById('walkingTrailMap');
                    const latitude = gpsCheck.latitude || 37.56661;  // 위도 값이 없으면 서울의 위도로 설정
                    const longitude = gpsCheck.longitude || 126.978388;  // 경도 값이 없으면 서울의 경도로 설정
                    const initialPosition = new naver.maps.LatLng(latitude, longitude); // 네이버 지도 좌표로 초기화
                    // 이미 초기화된 지도 객체 사용
                    const map = new naver.maps.Map(mapContainer, {
                        center: initialPosition,
                        zoom: 14,
                    });
                    const pathData = JSON.parse(data.path);
                    // 데이터에서 path 가져오기
                    const routePath = pathData.path.map(coord => {
                        const lat = coord.y || coord._lat; // y 또는 _lat을 lat으로 사용
                        const lng = coord.x || coord._lng; // x 또는 _lng을 lng으로 사용
                        return new naver.maps.LatLng(lat, lng);
                    });
                    // 경로를 따라 폴리라인을 그리기만 함 (API 요청 없이)
                    if (routePath.length >= 2) {
                        const newPolyline = new naver.maps.Polyline({
                            map: map,
                            path: routePath,
                            strokeColor: '#FF0000',
                            strokeWeight: 4,
                            strokeOpacity: 0.7
                        });
                        newPolyline.setMap(map);
                        // 폴리라인을 추가했지만 경로 거리 계산은 생략 (필요 시 거리 정보를 따로 계산 가능)
                    }
                })
                .catch(error => {
                    console.error('Error fetching routePicture:', error);
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
function getCurrentDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
}
function getCurrentHour() {
    const today = new Date();
    const hours = today.getHours(); // 현재 시간 (0-23)
    const minutes = today.getMinutes(); // 현재 분 (0-59)
    // 현재 시간에 대한 결과를 저장할 변수
    let adjustedHour;
    // 분이 10분 미만이면 전시간으로 설정
    if (minutes < 10) {
        adjustedHour = hours > 0 ? hours - 1 : 23; // 0시인 경우 23시로 설정
    } else {
        adjustedHour = hours; // 10분 이상이면 현재 시간으로 설정
    }
    // 2자리 형식으로 반환
    return String(adjustedHour).padStart(2, '0') + "00"; // 예: "0700", "0800"
}
function getWeatherInfo(nx, ny, latitude, longitude) {
    // 현재 날짜와 시간 가져오기
    const date = getCurrentDate();
    const hour = getCurrentHour();
    // AJAX 요청을 통해 날씨 정보 가져오기
    fetch(`/usr/walk/getWeather?date=${date}&hour=${hour}&nx=${nx}&ny=${ny}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json(); // JSON 형태로 응답을 가져오기
        })
        .then(data => {
            fetch(`/reverse-geocode?lat=${latitude}&lon=${longitude}`)
                .then(response => {
                    if(!response.ok){
                        throw new Error('Network response was not ok ' + response.statusText);
                    }
                    return response.text();
                })
                .then(data1 => {
                    const cityName = data1.split(" ")[0];
                    // obsrValue 값을 저장할 변수
                    let obsrValuePTY, obsrValueRN1, obsrValueT1H;
                    // items.item 배열이 있는지 확인하고 순회
                    const items = data.response.body.items.item; // item 배열 가져오기
                    if (Array.isArray(items)) {
                        items.forEach(item => {
                            const category = item.category; // category 값을 가져옴
                            const obsrValue = item.obsrValue; // obsrValue 값을 가져옴
                            // category에 따라 obsrValue 값 할당
                            if (category === "PTY") {
                                obsrValuePTY = obsrValue;
                            } else if (category === "RN1") {
                                obsrValueRN1 = obsrValue;
                            } else if (category === "T1H") {
                                obsrValueT1H = obsrValue;
                            }
                        });
                    }
                    // weatherIcon 객체를 이미지 파일로 수정
                    const weatherIcon = {
                        "0": "/resource/photo/sun-icon.png",  // 맑음
                        "1": "/resource/photo/rain-icon.png",  // 비
                        "2": "/resource/photo/rain-snow-icon.png", // 비/눈
                        "3": "/resource/photo/snow-icon.png",  // 눈
                        "4": "/resource/photo/storm-icon.png", // 천둥번개
                    };
                    // PTY에 따른 날씨 아이콘 출력
                    const weatherSymbol = weatherIcon[obsrValuePTY] || "❓"; // 기본 아이콘
                    const weatherSymbolElement = document.querySelector(".information"); // 날씨 아이콘을 표시할 요소
                    // "이 지역의 날씨" 멘트를 추가하고 아이콘을 다음 줄에 표시
                    weatherSymbolElement.innerHTML = `${cityName}의 날씨<br><img src="${weatherSymbol}" alt="Weather Icon" style="width: 21px; height: 21px;">`; // 아이콘을 이미지로 표시하고 크기 조정
                    // 기온 처리 및 표시
                    if (obsrValueT1H) {
                        const temperature = `${obsrValueT1H}°C`; // 섭씨 기호 붙이기
                        const temperatureElement = document.createElement('div');
                        temperatureElement.textContent = `기온: ${temperature}`;
                        weatherSymbolElement.appendChild(temperatureElement); // 날씨 아이콘 아래에 추가
                    }
                    // 강수량 처리 및 표시
                    if (obsrValueRN1 > 0) { // 강수량이 0 이상일 때만 표시
                        const rainfall = `${obsrValueRN1}mm`; // 강수량에 .mm 붙이기
                        const rainfallElement = document.createElement('div');
                        rainfallElement.textContent = `강수량: ${rainfall}`;
                        weatherSymbolElement.appendChild(rainfallElement); // 날씨 아이콘 아래에 추가
                    }
                });
        })
        .catch(error => {
            console.error('Error fetching weather data:', error);
        });
}