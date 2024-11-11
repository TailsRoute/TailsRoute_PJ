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
('2023-01-01 10:00:00', '2023-01-01 10:00:00', 1, 'First Diary Entry', 'Today I started my medication.', '/images/entry1.jpg', '2023-01-01', '2023-01-10', '08:00:00', 'Take with food.'),
('2023-01-02 11:00:00', '2023-01-02 11:00:00', 1, 'Second Diary Entry', 'Feeling good so far.', '/images/entry2.jpg', '2023-01-01', '2023-01-10', '08:00:00', 'No side effects noted.'),
('2023-01-03 12:00:00', '2023-01-03 12:00:00', 2, 'New Member Diary', 'Just started taking medication.', '/images/entry3.jpg', '2023-01-03', '2023-01-20', '09:00:00', 'Monitor for headaches.'),
('2023-01-04 13:00:00', '2023-01-04 13:00:00', 3, 'Medication Update', 'Had to change my dosage.', '/images/entry4.jpg', '2023-01-05', '2023-01-15', '07:30:00', 'Increase dosage as advised.'),
('2023-01-05 14:00:00', '2023-01-05 14:00:00', 1, 'Dietary Changes', 'Made some dietary changes to support my health.', '/images/entry5.jpg', '2023-01-01', '2023-01-10', '08:00:00', 'Avoid dairy while on medication.'),
('2023-01-06 15:00:00', '2023-01-06 15:00:00', 2, 'Feeling Tired', 'Noticed I am feeling more tired lately.', '/images/entry6.jpg', '2023-01-03', '2023-01-20', '09:00:00', 'Consult doctor if fatigue persists.'),
('2023-01-07 16:00:00', '2023-01-07 16:00:00', 3, 'Midway Check', 'Halfway through my medication course.', '/images/entry7.jpg', '2023-01-05', '2023-01-15', '07:30:00', 'Feeling hopeful about results.'),
('2023-01-08 17:00:00', '2023-01-08 17:00:00', 1, 'Final Days', 'Last few days of medication.', '/images/entry8.jpg', '2023-01-01', '2023-01-10', '08:00:00', 'Reflecting on my journey.'),
('2023-01-09 18:00:00', '2023-01-09 18:00:00', 2, 'Follow-up Appointment', 'Had a follow-up appointment today.', '/images/entry9.jpg', '2023-01-03', '2023-01-20', '09:00:00', 'Doctor is pleased with progress.'),
('2023-01-10 19:00:00', '2023-01-10 19:00:00', 3, 'Completion', 'Finished my medication course.', '/images/entry10.jpg', '2023-01-05', '2023-01-15', '07:30:00', 'Celebrate the achievement!');
