//  달력구현  //
document.addEventListener('DOMContentLoaded', function () {
    var calendarEl = document.getElementById('calendar');
    let selectedDate = null; // 선택된 날짜를 저장할 변수
    let selectedEvent = null; // 선택된 일정을 저장할 변수
    let selectedEventId = null; // 클릭한 일정의 ID 저장할 변수

    var calendar = new FullCalendar.Calendar(calendarEl, {
        timeZone: 'UTC',
        initialView: 'dayGridMonth',
        editable: false, // 일정을 드래그해서 편집할 수 없게 설정
        eventStartEditable: false, // 일정을 마우스로 시작 시간을 변경할 수 없게 설정
        eventDurationEditable: false, // 일정을 마우스로 길이를 조정할 수 없게 설정
        selectable: true,
        locale: 'ko',
        headerToolbar: {
            start: 'prev',
            center: 'title',
            end: 'customButton next'
        },
        customButtons: {
            customButton: {
                text: '일정 생성',
                click: function () {
                    // 일정 생성 팝업 열기
                    openPopup();
                }
            }
        },
        eventLimit: true, // 이 옵션을 추가하면 일정이 2개 이상일 때 "더보기..." 표시
        eventLimitClick: 'day', // "더보기..."를 클릭하면 일간 보기로 전환
        dayMaxEventRows: 2, // 하루에 표시할 최대 일정 수

// 달력 날짜 클릭 시 팝업 열고 날짜 저장
        dateClick: function (info) {
            selectedDate = info.dateStr; // 클릭한 날짜를 저장
        }
    });

    fetch(`/usr/essential/get?memberId=` + member.id) // memberId를 쿼리 파라미터로 추가
        .then(response => response.json())
        .then(data => {
            data.forEach(event => {
                const startDate = new Date(event.purchaseDate); // 구매 날짜를 시작일로 사용
                const endDate = new Date(startDate);
                endDate.setDate(startDate.getDate() + parseInt(event.usageCycle)); // 구매 주기에 따라 종료일 계산
                calendar.addEvent({
                    id: event.id, // 이벤트 ID 추가
                    title: event.itemType, // 생필품 이름
                    start: startDate.toISOString().split('T')[0], // 시작 날짜
                    end: endDate.toISOString().split('T')[0], // 종료 날짜 (주기에 따라)
                    allDay: true, // 하루 종일 일정
                    color: '#4D3E3E',
                    extendedProps: {
                        selecteDate: event.selecteDate,
                        usageCycle: event.usageCycle, // 구매 주기
                        timing: event.timing, // 알림 일수
                        purchaseStatus: event.purchaseStatus // 구매 상태
                    }
                });
            });
            calendar.render(); // 일정을 추가한 후에 달력 렌더링
        })
        .catch(error => {
            console.error('일정 불러오기 실패:', error);
        });

    // 달력에서 일정을 클릭했을 때 팝업을 여는 함수
    calendar.on('eventClick', function (info) {
        selectedEvent = info.event; // 클릭한 이벤트를 저장
        selectedEventId = info.event.id; // 클릭한 일정의 ID 저장
        const eventDate = selectedEvent.start; // 시작 날짜
        const formattedDate = eventDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식으로 변환
        document.getElementById("scheduleDate").value = formattedDate; // 팝업의 날짜 필드에 설정
        document.getElementById("itemName").value = selectedEvent.title; // 일정 제목을 팝업에 표시
        document.getElementById("purchaseCycle").value = selectedEvent.extendedProps.usageCycle || ''; // 구매 주기
        document.getElementById("alarmDays").value = selectedEvent.extendedProps.timing || ''; // 알림 일수

        // 팝업을 수정 모드로 열기
        openPopup(true); // true 인자를 통해 수정 모드 활성화
    });
    calendar.render();

    // 팝업 열기 및 닫기 기능
    const modal = document.getElementById("schedulePopup");
    const closeBtn = document.querySelector(".close");

    function openPopup(isEditMode = false) {
        // 팝업 열기
        document.getElementById("schedulePopup").style.display = "block";

        if (isEditMode) {
            // 수정 모드일 때
            document.getElementById("createScheduleBtn").style.display = "none"; // 일정 생성 버튼 숨김
            document.getElementById("updateScheduleBtn").style.display = "block"; // 일정 수정 버튼 보임
        } else {
            // 생성 모드일 때
            document.getElementById("createScheduleBtn").style.display = "block"; // 일정 생성 버튼 보임
            document.getElementById("updateScheduleBtn").style.display = "none"; // 일정 수정 버튼 숨김
        }
    }

    function closePopup() {
        // 팝업 닫기 로직 구현
        document.getElementById("schedulePopup").style.display = "none";

        // 입력 필드 초기화
        document.getElementById("itemName").value = '';
        // 필요에 따라 다른 입력 필드도 초기화

        selectedEvent = null; // 선택된 이벤트 초기화
        selectedEventId = null; // 선택된 이벤트 ID 초기화
    }

    closeBtn.onclick = function () {
        closePopup(); // 닫기 버튼 클릭 시 팝업 닫기
    };

    window.onclick = function (event) {
        if (event.target == modal) {
            closePopup(); // 모달 외부 클릭 시 팝업 닫기
        }
    };

    // 일정 생성 버튼 클릭 시
    document.getElementById("createScheduleBtn").addEventListener("click", function () {
        const selectedDate = document.getElementById("scheduleDate").value;
        const itemName = document.getElementById("itemName").value;
        const purchaseCycle = document.getElementById("purchaseCycle").value;
        const alarmDays = document.getElementById("alarmDays").value;

        // 필수 입력값 확인
        if (!itemName || !selectedDate) {
            alert("생필품 이름을 입력하세요.");
            return;
        }

        // 시작일 설정
        const startDate = new Date(selectedDate);

        // 주기를 일정 길이로 사용 (주기가 2라면 2일 동안 일정)
        const endDate = new Date(startDate); // 종료일 설정
        endDate.setDate(startDate.getDate() + parseInt(purchaseCycle)); // 주기를 반영한 종료일 설정

        // 선택한 날짜에 새로운 일정 추가
        calendar.addEvent({
            title: itemName,
            start: selectedDate, // 저장한 날짜를 사용
            end: endDate.toISOString().split('T')[0], // 종료일 (주기에 따라)
            allDay: true, // 하루 종일 일정
        });

        // 생필품 정보 객체 생성
        const essential = {
            memberId: member.id, // JSP에서 동적으로 회원 ID를 삽입
            itemType: itemName, // 생필품 이름
            purchaseDate: selectedDate, // 선택한 날짜
            usageCycle: purchaseCycle, // 선택한 주기
            timing: alarmDays // 알람 일수
        };

        // 생필품 정보를 서버에 전송
        sendEssentialInfo(essential);
        // 알람이 설정된 경우에만 알람 정보를 전송
        if (alarmDays) {
            sendAlarmInfo(selectedDate, itemName, alarmDays);
        }
    });

// 생필품 정보 전송 함수
    function sendEssentialInfo(essential) {
        fetch('/usr/essential/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(essential),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(data => {
                setTimeout(() => {
                    location.reload(); // 페이지 새로고침
                }, 5000); // 2초 후에 새로고침
                closePopup(); // 팝업 닫기
            })
            .catch(error => {
                console.error('There was a problem with the essential fetch operation:', error);
            });
    }

// 알람 정보 전송 함수
    function sendAlarmInfo(selectedDate, itemName, alarmDays) {
        // 알람 날짜 계산 (알람 일수 이전의 날짜로 설정)
        const alarmDate = new Date(selectedDate);
        alarmDate.setDate(alarmDate.getDate() - parseInt(alarmDays));

        // 알람 정보 객체 생성
        const alarm = {
            memberId: member.id, // JSP에서 동적으로 회원 ID를 삽입
            alarm_date: alarmDate.toISOString().split('T')[0], // 알람이 울릴 날짜
            message: `${itemName} 구매 예정일까지 ${alarmDays}일 남았습니다.`, // 알람 메시지
            site: `../shopping/page`
        };

        // 알람 정보를 서버에 전송
        fetch('/usr/alarm/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(alarm),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(data => {
                console.log('Alarm added:', data);
            })
            .catch(error => {
                console.error('There was a problem with the alarm fetch operation:', error);
            });
    }

// 일정 수정 버튼 클릭 시
    document.getElementById("updateScheduleBtn").addEventListener("click", function () {
        const purchaseDate = document.getElementById("scheduleDate").value;
        const itemName = document.getElementById("itemName").value;
        const purchaseCycle = document.getElementById("purchaseCycle").value;
        const alarmDays = document.getElementById("alarmDays").value;

        if (!itemName || !purchaseDate) {
            alert("생필품 이름과 구매일을 입력하세요.");
            return;
        }

        // 기존 일정을 삭제한 후 새로운 일정 추가
        deleteExistingSchedule(itemName, purchaseDate, purchaseCycle, alarmDays)
            .then(() => {
                // 일정 삭제가 완료된 후 새 일정 추가
                const startDate = new Date(purchaseDate);
                const endDate = new Date(startDate);
                endDate.setDate(startDate.getDate() + parseInt(purchaseCycle));

                // 새로운 일정 추가
                calendar.addEvent({
                    title: itemName,
                    start: purchaseDate,
                    end: endDate.toISOString().split('T')[0],
                    allDay: true,
                });

                // 생필품 정보 객체 생성
                const essential = {
                    memberId: member.id, // JSP에서 동적으로 회원 ID를 삽입
                    itemType: itemName, // 생필품 이름
                    purchaseDate: purchaseDate, // 선택한 날짜
                    usageCycle: purchaseCycle, // 선택한 주기
                    timing: alarmDays // 알람 일수
                };

                // 생필품 정보를 서버에 전송
                sendEssentialInfo(essential);

                // 알람이 설정된 경우에만 알람 정보를 전송
                if (alarmDays) {
                    sendAlarmInfo(purchaseDate, itemName, alarmDays);
                }
            })
            .catch(error => {
                console.error('일정 삭제 실패:', error);
            });
    });

// 기존 일정 삭제 함수
    function deleteExistingSchedule(itemName, purchaseDate, purchaseCycle, alarmDays) {
        return new Promise((resolve, reject) => {
            let eventIdToDelete;

            fetch(`/usr/essential/get?memberId=` + memberId)
                .then(response => response.json())
                .then(data => {
                    // 특정 이벤트를 찾기 위한 로직
                    data.forEach(event => {
                        const purchaseDates = new Date(event.purchaseDate);
                        const timingDays = event.timing;

                        // 알람 날짜 계산
                        const newDate = new Date(purchaseDates);
                        newDate.setDate(purchaseDates.getDate() - timingDays);
                        const formattedDate = newDate.toISOString().split('T')[0];

                        // itemName, purchaseDate, usageCycle, formattedDate 비교
                        if (event.itemType === itemName &&
                            event.purchaseDate === purchaseDate &&
                            Number(event.usageCycle) === Number(purchaseCycle) &&
                            (formattedDate === (alarmDays ? formattedDate : '알람 없음'))) {
                            eventIdToDelete = event.id; // 해당 ID를 저장
                        }
                    });

                    // 삭제할 이벤트 ID가 존재하면 삭제 요청
                    if (eventIdToDelete) {
                        // 알람 삭제 요청
                        deleteAlarmInfo(eventIdToDelete);

                        // DELETE 메소드로 요청 보내기
                        fetch(`/usr/essential/delete?id=` + eventIdToDelete, {
                            method: 'DELETE'
                        })
                            .then(response => {
                                if (!response.ok) {
                                    throw new Error('삭제 요청이 실패했습니다.');
                                }
                                resolve(); // 삭제 완료
                            })
                            .catch(error => {
                                reject(error);
                            });
                    } else {
                        resolve(); // 삭제할 일정이 없으면 그냥 완료
                    }
                })
                .catch(error => {
                    reject(error);
                });
        });
    }
                                              //  달력구현 끝  //
                                              //  리스트 구현  //
    let essentials = []; // 전역 변수로 essentials 정의
    let currentSortColumn = ''; // 현재 정렬된 열을 추적하기 위한 변수
    let isAscending = true; // 정렬 방향을 추적하기 위한 변수
    let memberId = member.id;
    fetchEssentials(memberId);

    // 데이터를 HTML로 렌더링하는 함수
    function renderEssentials(essentialsData) {
        const tableBody = document.querySelector("#essentialTable tbody");
        tableBody.innerHTML = ""; // 기존 내용을 지웁니다.

        essentialsData.forEach(item => {
            // 구매일을 Date 객체로 변환
            const purchaseDate = new Date(item.purchaseDate);

            // 알람 날짜를 계산 및 포맷팅
            let alarmDateFormatted = '';
            if (item.timing === 0) {
                alarmDateFormatted = "알람 없음"; // timing이 0일 경우 "알람 없음"으로 출력
            } else {
                const alarmDate = new Date(purchaseDate);
                alarmDate.setDate(purchaseDate.getDate() - item.timing); // timing을 빼기
                alarmDateFormatted = alarmDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식
            }
            const row = document.createElement("tr");
            row.innerHTML = `
            <td>${item.itemType}</td>
            <td>${item.purchaseDate}</td>
            <td>${item.usageCycle}일</td>
            <td>${alarmDateFormatted}</td>
            <td>
            <button class="delete-btn"
                        data-item-type="${item.itemType}"
                        data-purchase-date="${item.purchaseDate}"
                        data-usage-cycle="${item.usageCycle}"
                        data-alarm-date="${alarmDateFormatted}">삭제</button></td>
        `;

            // 클릭 이벤트 추가
            row.addEventListener("click", () => {
                const query = encodeURIComponent(item.itemType); // itemType을 URL에 안전한 형태로 변환
                searchProducts(query); // 검색 함수 호출
            });

            tableBody.appendChild(row);
        });
        // 삭제 버튼 클릭 이벤트 설정
        const deleteButtons = document.querySelectorAll(".delete-btn");
        deleteButtons.forEach(button => {
            button.addEventListener("click", function (event) {
                event.stopPropagation(); // 부모 요소의 클릭 이벤트가 발생하지 않도록 방지
                const itemName = this.getAttribute("data-item-type");
                const purchaseDate = this.getAttribute("data-purchase-date");
                const usageCycle = this.getAttribute("data-usage-cycle");
                const alarmDate = this.getAttribute("data-alarm-date");

                // 알림창 표시
                const confirmDelete = confirm("정말로 삭제하시겠습니까?");

                if (confirmDelete) {
                    // 사용자가 "예"를 선택했을 때만 삭제 함수 호출
                    deleteSchedule(itemName, purchaseDate, usageCycle, alarmDate);
                } else {
                    // 사용자가 "아니오"를 선택했을 때는 삭제 취소
                    console.log("삭제가 취소되었습니다.");
                }
            });
        });
    }

    // 삭제 일정 함수
    function deleteSchedule(itemName, purchaseDate, usageCycle, alarmDate) {
        let eventIdToDelete;

        if (!itemName) {
            alert("아이템 이름을 입력하세요.");
            return;
        }

        // memberId를 쿼리 파라미터로 추가하여 기존 일정 가져오기
        fetch(`/usr/essential/get?memberId=` + memberId)
            .then(response => response.json())
            .then(data => {
                // 특정 이벤트를 찾기 위한 로직
                data.forEach(event => {
                    // 구매일을 Date 객체로 변환
                    const purchaseDates = new Date(event.purchaseDate); // event.purchaseDate는 "2024-10-17" 형식
                    const timingDays = event.timing; // timing은 몇 일인지

                    // timing을 구매일에 빼서 새로운 날짜 계산
                    const newDate = new Date(purchaseDates);
                    newDate.setDate(purchaseDates.getDate() - timingDays); // timing일수를 빼기

                    const formattedDate = newDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식

                    // itemName, purchaseDate, usageCycle, formattedDate 비교
                    if (event.itemType === itemName &&
                        event.purchaseDate === purchaseDate &&
                        Number(event.usageCycle) === Number(usageCycle) && // == 사용, 데이터 형식이 다를 수 있음
                        (formattedDate === alarmDate || (alarmDate === '알람 없음' && event.timing === 0))) {
                        eventIdToDelete = event.id; // 해당 ID를 저장
                    }
                });

                // 삭제할 이벤트 ID가 존재하면 삭제 요청
                if (eventIdToDelete) {
                    // 알람 삭제 요청
                    deleteAlarmInfo(eventIdToDelete);

                    // DELETE 메소드로 요청 보내기
                    fetch(`/usr/essential/delete?id=` + eventIdToDelete, {
                        method: 'DELETE'
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('삭제 요청이 실패했습니다.');
                            }
                            return response.json(); // 응답을 JSON으로 처리
                        })
                        .then(data => {
                            console.log('삭제 성공:', data.message);
                            location.reload();
                        })
                        .catch(error => {
                            console.error('삭제 실패:', error);
                        });
                } else {
                    alert("해당 일정을 찾을 수 없습니다.");
                }
            })
            .catch(error => {
                console.error('일정 불러오기 실패:', error);
            });
    }

// 알람 정보 삭제 함수
    function deleteAlarmInfo(eventId) {
        // 알람 정보를 서버에 전송 (삭제 요청)
        fetch(`/usr/alarm/delete?id=` + eventId, {
            method: 'DELETE',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('알람 삭제 요청이 실패했습니다.');
                }
                return response.json(); // 응답을 JSON으로 처리
            })
            .then(data => {
                console.log('알람 삭제 성공:', data.message);
            })
            .catch(error => {
                console.error('알람 삭제 실패:', error);
            });
    }

    // 정렬 로직
    function sortByColumn(essentialsData, column, isAscending) {
        return essentialsData.sort((a, b) => {
            let valueA = a[column] || '';  // undefined 처리
            let valueB = b[column] || '';

            // 'timing' 값을 이용하여 알람일 계산
            if (column === 'alarmDays') {
                const today = new Date();
                const alarmDateA = new Date(today);
                alarmDateA.setDate(today.getDate() - a.timing);  // a의 timing을 사용하여 알람일 계산

                const alarmDateB = new Date(today);
                alarmDateB.setDate(today.getDate() - b.timing);  // b의 timing을 사용하여 알람일 계산

                valueA = alarmDateA;
                valueB = alarmDateB;
            }

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
        itemType: document.getElementById("itemTypeArrow"),
        purchaseDate: document.getElementById("purchaseDateArrow"),
        usageCycle: document.getElementById("usageCycleArrow"),
        alarmDays: document.getElementById("alarmDateArrow"),
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
        const sortedEssentials = sortByColumn(essentials, currentSortColumn, isAscending); // 정렬
        renderEssentials(sortedEssentials); // 정렬된 데이터 렌더링
    }

    init(); // 초기화 호출

// 품목 정렬
    document.getElementById("itemTypeHeader").addEventListener("click", () => {
        resetArrows(); // 화살표 초기화
        isAscending = currentSortColumn !== 'itemType' || !isAscending; // 정렬 방향 결정
        currentSortColumn = 'itemType';
        arrows.itemType.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
        arrows.itemType.classList.add('visible'); // 화살표 보이기
        const sortedEssentials = sortByColumn(essentials, 'itemType', isAscending); // 정렬
        renderEssentials(sortedEssentials); // 정렬된 데이터 렌더링
    });

// 구매일 정렬
    document.getElementById("purchaseDateHeader").addEventListener("click", () => {
        resetArrows(); // 화살표 초기화
        isAscending = currentSortColumn !== 'purchaseDate' || !isAscending; // 정렬 방향 결정
        currentSortColumn = 'purchaseDate';
        arrows.purchaseDate.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
        arrows.purchaseDate.classList.add('visible'); // 화살표 보이기
        const sortedEssentials = sortByColumn(essentials, 'purchaseDate', isAscending); // 정렬
        renderEssentials(sortedEssentials); // 정렬된 데이터 렌더링
    });

// 사용기한 정렬
    document.getElementById("usageCycleHeader").addEventListener("click", () => {
        resetArrows(); // 화살표 초기화
        isAscending = currentSortColumn !== 'usageCycle' || !isAscending; // 정렬 방향 결정
        currentSortColumn = 'usageCycle';
        arrows.usageCycle.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
        arrows.usageCycle.classList.add('visible'); // 화살표 보이기
        const sortedEssentials = sortByColumn(essentials, 'usageCycle', isAscending); // 정렬
        renderEssentials(sortedEssentials); // 정렬된 데이터 렌더링
    });

// 알람일 정렬
    document.getElementById("alarmDateHeader").addEventListener("click", () => {
        resetArrows(); // 화살표 초기화
        isAscending = currentSortColumn !== 'alarmDays' || !isAscending; // 정렬 방향 결정
        currentSortColumn = 'alarmDays';
        arrows.alarmDays.textContent = isAscending ? '▼' : '▲'; // 화살표 표시
        arrows.alarmDays.classList.add('visible'); // 화살표 보이기
        const sortedEssentials = sortByColumn(essentials, 'alarmDays', isAscending); // 정렬
        renderEssentials(sortedEssentials); // 정렬된 데이터 렌더링
    });

    // 품목 리스트를 가져오는 함수
    function fetchEssentials(memberId) {
        fetch(`/usr/essential/get?memberId=${member.id}`) // memberId 값을 URL에 추가
            .then(response => {
                if (!response.ok) {
                    throw new Error('데이터를 가져오는 데 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                essentials = data; // 데이터를 essentials에 할당

                // 기본적으로 구매일 순으로 정렬
                essentials = sortByColumn(essentials, 'purchaseDate', true); // 오름차순 정렬
                renderEssentials(essentials); // 데이터를 렌더링하는 함수 호출
            })
            .catch(error => {
                console.error('Error fetching essentials:', error);
            });
    }
});
                                                             //  리스트 구현 끝  //
                                                             //  검색 구현  //
let products = []; // 전역 변수로 상품 배열을 선언
let currentPage = 1; // 현재 페이지
const itemsPerPage = 5; // 페이지당 아이템 수

// 페이지 로드 시 자동으로 검색
document.addEventListener("DOMContentLoaded", function () {
    const query = "용품";  // 예: "펫 용품"
    searchProducts(query); // 초기 검색 실행
});
function searchItems() {
    const query = document.getElementById("searchInput").value;
    currentPage = 1;
    searchProducts(query)
}

let originalProducts = []; // 원본 데이터 저장
// 제품 검색 함수
function searchProducts(query, sort = '') {
    // 검색어를 디코딩하여 사람이 읽을 수 있게 만듦
    let decodedQuery = decodeURIComponent(query);

    if(decodedQuery==='용품'){
        decodedQuery = '펫' + decodedQuery;
    }

    // 제목에 검색어 추가
    document.getElementById("searchTitle").textContent = `쇼핑 추천 리스트 - 검색 결과 : ${decodedQuery}`;
    // API 요청 보내기
    fetch('/searchProducts?query=' + query + '&display=' + 100 + sort)
        .then(response => response.json())
        .then(data => {
            products = data.items.filter(item => item.category2 === "반려동물"); // 전체 상품 데이터를 저장
            originalProducts = [...products]; // 원본 데이터를 복사하여 저장
            displayResults(products); // 결과 표시
            setupPagination(products.length); // 페이지네이션 설정
        })
        .catch(error => console.error('Error fetching data:', error));
}

// 결과를 HTML에 표시하는 함수
function displayResults(data) {
    const resultContainer = document.getElementById("resultContainer");
    resultContainer.innerHTML = '';  // 기존 내용 초기화

    // 페이지에 따라 데이터 필터링
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedData = data.slice(startIndex, endIndex);

    // 각 상품을 화면에 표시
    paginatedData.forEach(item => {
        const productItem = document.createElement('li'); // li 요소 생성
        productItem.classList.add('product-item'); // 클래스 추가
        productItem.innerHTML =
            `<img src="${item.image}" alt="${item.title}" class="product-image">
                        <div class="product-info" style="display: flex; justify-content: space-between; align-items: center; width: 100%">
                        <div>
                            <a href="${item.link}">${item.title}</a>
                            <br>
                            <a href="${item.link}" class="product-price">가격: ${item.lprice} 원</a>
                            </div>
                        <button id="addToCartButton" onclick="addToCart()">관심목록에 추가</button></div>`;
        resultContainer.appendChild(productItem); // 리스트에 추가
    });
}

// 페이지네이션 설정 함수
function setupPagination(totalItems) {
    const pageCount = Math.ceil(totalItems / itemsPerPage);
    const pageInfo = document.getElementById("pageInfo");
    pageInfo.innerHTML = `${currentPage} / ${pageCount}`; // 현재 페이지 정보 표시
}
let cart = [];

// 상품을 장바구니에 추가하는 함수
function addToCart() {
    const productItem = event.target.closest('.product-item');
    const productTitle = productItem.querySelector('a').textContent;
    const productPrice = parseInt(productItem.querySelector('.product-price').textContent.replace(/[^0-9]/g, ''), 10);

    const product = {
        memberId: member.id,
        itemName: productTitle,
        itemprice: productPrice
    };

    fetch('/usr/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
    })
        .then(response => response.ok ? alert(`${productTitle}이(가) 관심목록에 추가되었습니다.`) : Promise.reject())
        .catch(console.error);
}

// 장바구니 팝업을 열고 내용을 표시하는 함수
function showCartPopup() {
    const cartPopup = document.getElementById('cartPopup');
    const cartItemsList = document.getElementById('cartItems');
    cartItemsList.innerHTML = '';  // 초기화

    fetch(`/usr/cart/get?memberId=${member.id}`)
        .then(response => response.json())
        .then(cartData => {
            if (cartData.length === 0) {
                cartItemsList.innerHTML = '<li>관심목록에 제품이 없습니다.</li>';
            } else {
                cartData.forEach(item => {
                    const listItem = document.createElement('li');
                    listItem.innerHTML = `
                        <div style="display: flex; justify-content: space-between; align-items: center; height: 50px; width:450px; border-bottom: 1px solid #ccc; margin-bottom: 10px;">
                            <span style="width: 400px;">${item.itemName} - ${item.itemprice}원</span>
                            <button style="display: inline-block; padding: 0;">삭제</button>
                        </div>`;
                    listItem.querySelector('button').addEventListener('click', () => {
                        if (confirm('정말로 삭제하시겠습니까?')) {
                            removeCartItem(item.id);
                        }
                    });
                    cartItemsList.appendChild(listItem);
                });
            }
        })
        .catch(error => {
            cartItemsList.innerHTML = '<li>장바구니 데이터를 가져오는 중 오류가 발생했습니다.</li>';
            console.error(error);
        });

    cartPopup.style.display = 'block';
}

// 장바구니 항목 삭제
function removeCartItem(cartItemId) {
    fetch(`/usr/cart/delete?id=${cartItemId}`, { method: 'DELETE' })
        .then(response => response.ok ? showCartPopup() : console.error('Failed to delete cart item'))
        .catch(console.error);
}

// 팝업 닫기
function closeCartPopup() {
    document.getElementById('cartPopup').style.display = 'none';
}

document.getElementById('wishlistButton').addEventListener('click', showCartPopup);

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('closeCartPopup')?.addEventListener('click', closeCartPopup);
});

window.onclick = function(event) {
    if (event.target === document.getElementById('cartPopup')) {
        closeCartPopup();
    }
};

// 페이지 변경 함수
function changePage(direction) {
    const pageCount = Math.ceil(products.length / itemsPerPage);
    if (direction === 'next' && currentPage < pageCount) {
        currentPage++;
    } else if (direction === 'prev' && currentPage > 1) {
        currentPage--;
    }
    displayResults(products); // 필터링된 결과를 다시 표시
    setupPagination(products.length); // 페이지네이션 정보 업데이트
}

// 버튼 클릭 이벤트 설정
document.getElementById("nextPage").addEventListener("click", () => changePage('next'));
document.getElementById("prevPage").addEventListener("click", () => changePage('prev'));

// 정렬 기능 설정
document.getElementById("sortByAccuracy").addEventListener("click", () => {
    products = [...originalProducts]; // 원본 데이터로 돌아감
    displayResults(products); // 정렬된 결과 표시
    setActiveButton("sortByAccuracy"); // 버튼 스타일 변경
});

document.getElementById("sortByPrice").addEventListener("click", () => {
    products.sort((a, b) => a.lprice - b.lprice); // 가격순 정렬
    displayResults(products); // 정렬된 결과 표시
    setActiveButton("sortByPrice"); // 버튼 스타일 변경
});
// 버튼 활성화 상태 변경 함수
function setActiveButton(activeButtonId) {
    // 모든 버튼의 active 클래스 제거
    document.querySelectorAll("#sortButtons button").forEach(button => {
        button.classList.remove("active");
    });

    // 클릭된 버튼에 active 클래스 추가
    document.getElementById(activeButtonId).classList.add("active");
}