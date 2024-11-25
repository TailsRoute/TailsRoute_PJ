## 일지작성  테이블
CREATE TABLE Diary(
                      Id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '일지 고유번호',
                      regDate DATETIME NOT NULL COMMENT '생성일',
                      updateDate DATETIME NOT NULL COMMENT'수정일',
                      memberId INT(10) NOT NULL COMMENT'회원번호',
                      title CHAR(200) NOT NULL COMMENT'제목',
                      `body` TEXT NOT NULL COMMENT'내용',
                      imagePath CHAR(200) NOT NULL COMMENT '이미지 저장경로',
                      startDate DATE NOT NULL COMMENT '약복용 시작일',
                      endDate DATE NOT NULL COMMENT '약복용 종료일',
                      takingTime TIME NOT NULL COMMENT '복용 시간',
                      information TEXT NOT NULL COMMENT '복용약 특이사항'
);

## 일지작성 테스트데이터
INSERT INTO Diary (regDate, updateDate, memberId, title, BODY, imagePath, startDate, endDate, takingTime, information) VALUES
('2024-11-01 10:00:00', '2024-11-01 10:00:00', 1, '첫 산책: 새로운 코스를 탐험하다', '오늘 반려견과 함께 새로 발견한 공원 코스를 걸었다. 반응이 좋았고, 공원에는 다양한 강아지 친구들이 있었다.', '/uploads/diary/default.png', '2024-11-01', '2024-11-10', '08:00:00', '약 복용(타이레놀) 후 충분히 물을 제공.'),
('2024-11-02 11:00:00', '2024-11-02 11:00:00', 1, '두 번째 산책: 조용한 아침', '아침 일찍 동네 산책로를 따라 걸었다. 반려견이 조금 더 활발해 보였고, 아침 공기가 맑아 기분이 좋았다.', '/uploads/diary/default2.jpg', '2024-11-11', '2024-11-12', '08:00:00', '약 복용(비타민C) 후 산책 전 간단한 스트레칭 권장.'),
('2024-11-05 14:00:00', '2024-11-05 14:00:00', 1, '비 온 뒤 산책: 상쾌한 공기', '비가 그친 후 맑은 하늘 아래 산책. 반려견이 흙냄새를 맡으며 활발히 움직였다. 오늘은 평소보다 더 많은 거리를 걸었다.', '/uploads/diary/default3.png', '2024-11-13', '2024-11-14', '08:00:00', '약 복용(유산균) 중 간식 제공은 소량으로 제한.'),
('2024-11-08 17:00:00', '2024-11-08 17:00:00', 1, '저녁 산책: 황혼 속 반려견', '노을 사진 촬영 후 20분 간 걷기. 반려견이 주변을 유심히 살피며 천천히 걸었다. 오늘은 리드줄 훈련을 추가로 진행했다.', '/uploads/diary/default4.jpeg', '2024-11-15', '2024-11-16', '08:00:00', '약 복용(오메가3) 후 충분히 물을 제공.');