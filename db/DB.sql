DROP DATABASE IF EXISTS `tails_route`;
CREATE DATABASE `tails_route`;
USE `tails_route`;

## 회원정보 테이블
CREATE TABLE `member`(
                         id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                         regDate DATETIME NOT NULL COMMENT '가입 날짜',
                         updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                         loginId VARCHAR(250) NOT NULL COMMENT '아이디',
                         loginPw CHAR(100) NOT NULL COMMENT '비밀번호',
                         authLevel SMALLINT(2) UNSIGNED DEFAULT 3 COMMENT '권한 레벨 (3=일반, 7=관리자)',
                         `name` CHAR(20) NOT NULL COMMENT '오프라인 이름',
                         nickname CHAR(20) NOT NULL COMMENT '온라인 이름',
                         cellphoneNum CHAR(20) NOT NULL COMMENT '전화번호',
                         email CHAR(50) NOT NULL COMMENT '이메일',
                         socialLoginStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '소셜 로그인 여부 (0=일반 회원, 1=소셜 로그인 회원)',
                         delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
                         delDate DATETIME COMMENT '탈퇴 날짜'
);

## 회원정보 테이블 테스트 데이터
INSERT INTO `member` SET
                         regDate = '2024-02-05 14:15:00',
                         updateDate = '2024-03-10 16:30:00',
                         loginId = 'admin',
                         loginPw = 'pw_hash2',
                         authLevel = 7,
                         `name` = '관리자',
                         nickname = '강아지왕',
                         cellphoneNum = '010-8765-4321',
                         email = "admin@TailsRoute.site";

INSERT INTO `member` SET
    regDate = '2024-01-10 10:30:00',
                         updateDate = '2024-02-10 12:00:00',
                         loginId = 'user01',
                         loginPw = 'pw_hash1',
                         authLevel = 3,
                         `name` = '김서준',
                         nickname = '콩이의대장',
                         cellphoneNum = '010-1234-5678',
                         email = "user01@TailsRoute.site";


INSERT INTO `member` SET
    regDate = '2024-03-20 09:45:00',
                         updateDate = '2024-04-25 14:15:00',
                         loginId = 'user02',
                         loginPw = 'pw_hash3',
                         authLevel = 3,
                         `name` = '이지아',
                         nickname = '바둑이의수호자',
                         cellphoneNum = '010-1111-2222',
                         email = "user02@TailsRoute.site",
                         delStatus = 1,
                         delDate = '2024-05-01 10:30:00';

INSERT INTO `member` SET
    regDate = '2024-04-18 16:00:00',
                         updateDate = '2024-05-20 09:00:00',
                         loginId = 'qwe',
                         loginPw = 'qwe',
                         authLevel = 3,
                         `name` = '박도윤',
                         nickname = '두부는나의행복',
                         cellphoneNum = '010-3333-4444',
                         email = "qwe@TailsRoute.site";

INSERT INTO `member` SET
    regDate = '2024-05-22 11:30:00',
                         updateDate = '2024-06-10 15:45:00',
                         loginId = 'zxc',
                         loginPw = 'zxc',
                         authLevel = 3,
                         `name` = '최하은',
                         nickname = '뭉치의천사',
                         cellphoneNum = '010-5555-6666',
                         email = "zxc@TailsRoute.site";

INSERT INTO `member` SET
    regDate = '2024-07-22 12:20:00',
                         updateDate = '2024-08-01 12:40:00',
                         loginId = 'asd',
                         loginPw = 'asd',
                         authLevel = 3,
                         `name` = '유은희',
                         nickname = '꾸미엄마',
                         cellphoneNum = '010-7698-1532',
                         email = "asd@naver.com";


## 반려견 테이블
CREATE TABLE `dog`(
                      id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                      regDate DATETIME NOT NULL COMMENT '생성 날짜',
                      updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                      memberId INT(10) UNSIGNED NOT NULL COMMENT '주인 식별번호',
                      `name` CHAR(20) NOT NULL DEFAULT '이름 없음' COMMENT '이름',
                      weight CHAR(20) NOT NULL DEFAULT '불명' COMMENT '체중',
                      photo CHAR(50) NOT NULL COMMENT '사진',
                      `type` CHAR(20) NOT NULL COMMENT '소형, 중형, 대형',
                      comPortName CHAR(20) COMMENT 'GPS 기기 연결 포트'
);

INSERT INTO dog SET
    regDate = '2024-01-01 10:00:00',
                    updateDate = '2024-01-01 10:00:00',
                    memberId = 1,
                    weight = 5,
                    photo = '/resource/photo/dog1.png',
                    `type` = '소형';

INSERT INTO dog SET
    regDate = '2024-02-15 14:30:00',
                    updateDate = '2024-02-15 14:30:00',
                    memberId = 3,
                    `name` = '바둑이',
                    photo = '/resource/photo/dog2.png',
                    `type` = '중형';

INSERT INTO dog SET
    regDate = '2024-03-20 09:15:00',
                    updateDate = '2024-03-20 09:15:00',
                    memberId = 5,
                    `name` = '뭉치',
                    weight = 15,
                    photo = '/resource/photo/dog3.png',
                    `type` = '대형';

INSERT INTO dog SET
    regDate = '2024-04-25 16:45:00',
                    updateDate = '2024-04-25 16:45:00',
                    memberId = 6,
                    `name` = '꾸미',
                    weight = 4,
                    photo = '/resource/photo/dog4.png',
                    `type` = '소형',
                    comPortName = "COM7";

## 게시글 테이블
CREATE TABLE article(
                        id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                        regDate DATETIME NOT NULL COMMENT '작성 날짜',
                        updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                        memberId INT(10) UNSIGNED NOT NULL COMMENT '작성자 식별번호',
                        boardId INT(10) UNSIGNED NOT NULL COMMENT '게시판 식별번호',
                        title CHAR(100) NOT NULL COMMENT '제목',
                        `body` TEXT NOT NULL COMMENT '내용',
                        hitCount INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '조회수',
                        goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '좋아요',
                        badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '싫어요'
);

INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 2, title = '우리 리트리버, 바다에서 첫 수영을 하다! 🏖️🐶', `body` = '<p><img src="/uploads/photo/1732102471619_article3.png" contenteditable="false">안녕하세요, 우리 리트리버가 드디어 첫 수영을 했습니다! 🌊🏖</p><p><br></p><p><br></p><p>평소 물을 무서워하는 아이였는데, 이번에는 용기를 내어 바다로 뛰어들었어요! 처음엔 조심스럽게 물에 들어가더니, 금방 익숙해져서 신나게 물장구를 치기 시작했습니다. 강아지 수영하는 모습이 너무 귀여워서 웃음이 절로 나더라고요. 😂💕 사진도 몇 장 찍었는데, 나중에 꼭 공유할게요!</p><p>다른 반려견 친구들도 바다에서 수영할 때 팁이 있으면 알려주세요~ 😊</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 3, title = '첫 산책 전 : 낯선 환경에서의 반응 기록 🐱‍👓', `body` = '<p><img src="/uploads/photo/1732102438099_article2.png" contenteditable="false">반려견과 처음으로 공원 산책을 나갔습니다. 산책 중 새로운 환경에 대한 반응이 궁금해서 다양한 상황을 관찰했습니다. 처음에는 낯선 소리와 냄새에 놀라 조금 긴장했지만, 시간이 지날수록 주변에 익숙해졌습니다. 다른 강아지들과의 상호작용도 점차 자연스러워졌고, 중간에 잠시 앉아 쉴 때는 편안해 보였습니다. 이번 산책은 적응 능력을 키우는 좋은 경험이 되었습니다.</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 3, title = '강아지가 좋아하는 간식 추천해주세요!', `body` = '<p><img src="/uploads/photo/1732102618165_article7.png" contenteditable="false">우리 강아지가 간식을 좋아하는데, 건강에도 좋은 간식을 찾고 있어요. 혹시 추천해주실 만한 간식이 있을까요?</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 2, title = '우리 강아지 첫 산책, 너무 행복해요!🙌', `body` = '<p><img src="/uploads/photo/1732102563598_article5.png" contenteditable="false">오늘 드디어 우리 강아지와 첫 산책을 했어요. 처음에는 좀 긴장했지만, 금방 적응하더니 신나게 뛰어다녔어요. 너무 귀여워서 행복한 시간이었어요!</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 3, title = '강아지  예방접종 다 했나요?', `body` = '<p><img src="/uploads/photo/1732102639820_article8.png" contenteditable="false">강아지 예방접종은 언제 시작해야 할지 고민이에요. 다들 강아지 예방접종을 어떻게 하셨나요? 어떤 접종이 꼭 필요한지 알려주세요!</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 2, title = '우리 강아지, 첫 번째 생일 축하해요!', `body` = '<p><img src="/uploads/photo/1732102541582_article4.png" contenteditable="false">오늘은 우리 강아지의 첫 번째 생일이에요! 너무 귀엽고 사랑스러운 존재라서 매일매일 행복합니다. 생일 파티도 해주고 특별한 간식도 준비했어요. 너무 자랑스러워요!</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 2, title = '우리 강아지, 오늘도 멋진 포즈🐱‍🏍', `body` = '<p><img src="/uploads/photo/1732102594990_article6.png" contenteditable="false">오늘도 우리 강아지가 멋진 포즈로 앉아 있었어요. 뒷다리 쭉 펴고 앉아 있는 모습이 정말 귀여워서 사진 찍어봤어요. 이런 모습 보면 하루가 정말 기분 좋아요.</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 3, title = '우리 강아지, 너무 많은 장난감을 좋아해요!', `body` = '<p><img src="/uploads/photo/1732102406879_article1.png" contenteditable="false">강아지가 장난감을 너무 많이 좋아해서 집에 장난감이 가득해요. 다들 강아지 장난감은 얼마나 자주 사주시나요?</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 3, title = '강아지가 처음 만나는 사람을 너무 무서워해요 😢', `body` = '<p>우리 강아지가 처음 만나는 사람을 너무 무서워해서, 사회화가 걱정돼요. 어떻게 잘 적응시킬 수 있을까요?</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);
INSERT INTO article SET regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND), memberId = FLOOR(1 + RAND() * 6), boardId = 2, title = '우리 강아지, 너무 잘 배워요!', `body` = '<p>우리 강아지가 "앉아", "기다려", "손" 같은 기본 명령어를 너무 빨리 배웠어요. 정말 똑똑하고 순종적이라 자랑스럽습니다!</p>', hitCount = FLOOR(1 + RAND() * 100), goodReactionPoint = FLOOR(1 + RAND() * 100);

## 게시판 테이블
CREATE TABLE board(
                      id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                      regDate DATETIME NOT NULL COMMENT '생성 날짜',
                      updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                      `code` CHAR(100) NOT NULL UNIQUE COMMENT 'notice(공지사항) free(자유) Q&A(질의응답)',
                      `name` CHAR(20) NOT NULL UNIQUE COMMENT '이름',
                      delStatus TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
                      delDate DATETIME COMMENT '삭제 날짜'
);

## 게시판 테스트 데이터
INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'NOTICE',
`name` = '공지사항';

INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'FREE',
`name` = '자유';

INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'QnA',
`name` = '질의응답';

INSERT INTO board
SET regDate = NOW(),
updateDate = NOW(),
`code` = 'recommend',
`name` = '추천합니다';

## 리액션(좋아요, 싫어요) 테이블
CREATE TABLE reactionPoint(
                              id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                              regDate DATETIME NOT NULL COMMENT '추천 날짜',
                              updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                              memberId INT(10) UNSIGNED NOT NULL COMMENT '추천자 식별번호',
                              relTypeCode CHAR(20) NOT NULL COMMENT '추천대상 식별코드',
                              relId INT(10) UNSIGNED NOT NULL COMMENT '추천대상 식별번호',
                              `point` INT(10) COMMENT '좋아요, 싫어요 여부'
);

## 댓글 테이블
CREATE TABLE reply (
                       id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                       regDate DATETIME NOT NULL COMMENT '작성 날짜',
                       updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                       memberId INT(10) UNSIGNED NOT NULL COMMENT '작성자 식별번호',
                       relTypeCode CHAR(50) NOT NULL COMMENT '작성대상 식별코드',
                       relId INT(10) UNSIGNED NOT NULL COMMENT '작성대상 식별번호',
                       `body` TEXT NOT NULL COMMENT '내용',
                       goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '좋아요',
                       badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '싫어요'
);

## 알람 테이블
CREATE TABLE alarm (
                       id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                       regDate DATETIME NOT NULL COMMENT '생성 날짜',
                       updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                       memberId INT(10) UNSIGNED NOT NULL COMMENT '생성자 식별번호',
                       alarm_date DATE COMMENT '알람이 울릴 날짜',
                       alarm_day ENUM('월요일', '화요일', '수요일', '목요일', '금요일', '토요일', '일요일') COMMENT '알람이 울릴 요일',
                       message TEXT NOT NULL COMMENT '알람 메시지',
                       site TEXT NOT NULL COMMENT '어느 사이트에서 왔는지 여부',
                       is_sent BOOLEAN DEFAULT FALSE COMMENT '알람이 이미 전송되었는지 여부',
                       FOREIGN KEY (memberId) REFERENCES MEMBER(id),
                       CHECK ((alarm_date IS NOT NULL) OR (alarm_day IS NOT NULL))
);

## 생필품 테이블
CREATE TABLE essentials (
                            id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                            regDate DATETIME NOT NULL COMMENT '생성 날짜',
                            updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                            memberId INT(10) UNSIGNED NOT NULL COMMENT '생성자 식별번호',
                            itemType CHAR(50) NOT NULL COMMENT '생필품 종류',
                            productPicture TEXT NOT NULL COMMENT '생필품 사진',
                            productPrice TEXT NOT NULL COMMENT '생필품 가격',
                            productLink TEXT NOT NULL COMMENT '생필품 링크',
                            purchaseDate DATE NOT NULL COMMENT '구매 날짜',
                            usageCycle INT(10) NOT NULL COMMENT '사용주기',
                            timing INT(10) NOT NULL COMMENT '알림 시기'
);

## 약 체크 테이블
CREATE TABLE medicationLog (
                               id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                               regDate DATETIME NOT NULL COMMENT '생성 날짜',
                               memberId INT(10) UNSIGNED NOT NULL COMMENT '사용자 식별번호',
                               medicationDate DATE NOT NULL COMMENT '복용 날짜'
);

## 실종 테이블
CREATE TABLE missing(
                        id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                        memberId INT(10) UNSIGNED NOT NULL COMMENT '신고자 식별번호',
                        `name` CHAR(30) NOT NULL COMMENT '강아지 이름',
                        reportDate DATETIME NOT NULL COMMENT '실종 날짜',
                        missingLocation VARCHAR(100) NOT NULL COMMENT '실종 장소',
                        breed CHAR(30) NOT NULL COMMENT '품종',
                        color CHAR(30) NOT NULL COMMENT '색상',
                        gender CHAR(30) NOT NULL COMMENT '성별',
                        age CHAR(30) DEFAULT '불명' COMMENT '나이',
                        photo TEXT NOT NULL COMMENT '사진',
                        RFID CHAR(30) DEFAULT '없음' COMMENT '마이크로칩 번호',
                        trait TEXT NOT NULL COMMENT '특징'
);

## 실종 테스트 데이터
INSERT INTO `missing` SET
                          memberId = 1,
                          `name` = '바둑이',
                          reportDate = '2024-10-05 09',
                          missingLocation  = '경기도 포천시 창수면 옥수로 320-64',
                          breed = '믹스견',
                          color = 'BLACK TAN&WHITE',
                          gender = '암컷',
                          age = '108',
                          photo = '/resource/photo/missing1.png',
                          trait = '흰 바탕에 표범같은 작은 점들이 많이 있고 귀 엉덩이 허리부분에 진갈색 과 검정색이 섞인 큰 얼룩이 있어요';

INSERT INTO `missing` SET
    memberId = 2,
                          `name` = '몽이',
                          reportDate = '2024-10-10 08',
                          missingLocation  = '서울특별시 강서구 강서로45길 113 (내발산동)올라가는길',
                          breed = '포메라니안',
                          color = '흰색',
                          gender = '암컷',
                          age = '192',
                          photo = '/resource/photo/missing2.png',
                          trait = '포메스피츠 믹스견';

INSERT INTO `missing` SET
    memberId = 3,
                          `name` = '콩이',
                          reportDate = '2024-10-10 09',
                          missingLocation  = '강원특별자치도 강릉시 주문진읍 신리천로 4-1 (해안연립)19동201호',
                          breed = '말티즈',
                          color = '흰색',
                          gender = '수컷',
                          age = '7',
                          photo = '/resource/photo/missing3.png',
                          RFID = '410100012908279',
                          trait = '흰색 미니 칩심어져있음 사람에게 호의적';

INSERT INTO `missing` SET
    memberId = 4,
                          `name` = '로또',
                          reportDate = '2024-10-06 20',
                          missingLocation  = '전북특별자치도 김제시 부량면 벽골제로 320-13벽골제 지평선축제장',
                          breed = '시바',
                          color = '흑갈색',
                          gender = '수컷',
                          age = '34',
                          photo = '/resource/photo/missing4.png',
                          RFID = '410160010746866',
                          trait = '전체적으로 갈색검정털에 입주위 가슴 등쪽 날개모양 흰털 검은코 동그랗게 말린 꼬리';

INSERT INTO `missing` SET
    memberId = 5,
                          `name` = '하루',
                          reportDate = '2024-10-05 16',
                          missingLocation  = '경상남도 남해군 설천면 설천로775번길 256-17남해양떼목장양모리학교',
                          breed = '보더 콜리',
                          color = '검정&흰색',
                          gender = '수컷',
                          photo = '/resource/photo/missing5.png',
                          trait = '오른쪽뒷다리를다쳐서 절음';

INSERT INTO `missing` SET
    memberId = 6,
                          `name` = '멍이',
                          reportDate = '2024-10-05 22',
                          missingLocation  = '울산광역시 동구 등대로 95 (일산동)대왕암공원 주차장 일대',
                          breed = '비글',
                          color = '세가지색',
                          gender = '수컷',
                          age = '10',
                          photo = '/resource/photo/missing6.png',
                          trait = '목뒤 M자 무늬가 있음';

INSERT INTO `missing` SET
    memberId = 1,
                          `name` = '구름이',
                          reportDate = '2024-10-05 07',
                          missingLocation  = '대전광역시 중구 보문로 341 (선화동, 현대아파트)101동',
                          breed = '기타',
                          color = '흑색&회색',
                          gender = '암컷',
                          age = '46',
                          photo = '/resource/photo/missing7.png',
                          trait = '사람을 엄청나게 경계함 지금은 사진보다 털이 많이 자란상태임';

INSERT INTO `missing` SET
    memberId = 2,
                          `name` = '누비',
                          reportDate = '2024-10-03 10',
                          missingLocation  = '경기도 김포시 하성면 원통로28번길 37',
                          breed = '기타',
                          color = '흰색',
                          gender = '수컷',
                          age = '52',
                          photo = '/resource/photo/missing8.png',
                          trait = '폼피츠 이고 특별한 특징은 없읍니다';

INSERT INTO `missing` SET
    memberId = 3,
                          `name` = '모카',
                          reportDate = '2024-09-28 15',
                          missingLocation  = '경상북도 문경시 굴모리길 14 (불정동)초록울타리집',
                          breed = '진도견',
                          color = '황색',
                          gender = '암컷',
                          age = '148',
                          photo = '/resource/photo/missing9.png',
                          trait = '귀 속에 진도견인증도장찍혀있음 ㆍ선꼬리';

INSERT INTO `missing` SET
    memberId = 4,
                          `name` = '초코',
                          reportDate = '2024-09-25 06',
                          missingLocation  = '전라남도 강진군 강진읍 사의재길 31-23동성리 전 침례교회 아래집',
                          breed = '믹스견',
                          color = '황갈색',
                          gender = '수컷',
                          age = '96',
                          photo = '/resource/photo/missing10.png',
                          trait = '이름 만득이 진도 믹스 파랑빨강 목줄 꼬리 말아 올려져 있음 다리 길고 귀 쫑긋 털이 지저분함';

INSERT INTO `missing` SET
    memberId = 5,
                          `name` = '복실이',
                          reportDate = '2024-09-20 06',
                          missingLocation  = '충청북도 청주시 흥덕구 풍년로198번길 45-6 (가경동)3층',
                          breed = '말티즈',
                          color = '흰색',
                          gender = '수컷',
                          age = '122',
                          photo = '/resource/photo/missing11.png',
                          trait = '심장이 안좋아 자꾸 켁켁 거려요';

##건강기록 테이블
CREATE TABLE doghealth(
                          memberId INT(10) UNSIGNED NOT NULL COMMENT 'member번호',
                          dogWeight FLOAT NOT NULL COMMENT '강아지 체중',
                          vaccinationDate DATETIME NOT NULL COMMENT '예방 접종 날짜',
                          checkupDate DATETIME NOT NULL COMMENT '건강 검진 날짜',
                          activityLevel FLOAT NOT NULL COMMENT '활동량(평균걸음수)'
);

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

##반려견 행동범위 지정 테이블
CREATE TABLE gpsAlert(
                         id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                         regDate DATETIME NOT NULL COMMENT '등록 날짜',
                         updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                         dogId INT(10) UNSIGNED NOT NULL COMMENT '반려견 식별번호',
                         latitude DECIMAL(10, 7) NOT NULL COMMENT '설정한 위도',
                         longitude DECIMAL(10, 7) NOT NULL COMMENT '설정한 경도',
                         chack INT(1) UNSIGNED DEFAULT 0 COMMENT '범위 벗어났는지 여부',
                         onOff INT(1) UNSIGNED DEFAULT 0 COMMENT 'gps 알림 온오프 여부'
);

## 병원 테이블
CREATE TABLE hospital(
                         id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '고유 병원 ID',
                         `name` TEXT NOT NULL COMMENT '병원 이름',
                         callNumber VARCHAR(20) DEFAULT NULL COMMENT '소재지전화번호',
                         jibunAddress TEXT COMMENT '병원의 지번 주소',
                         roadAddress TEXT COMMENT '병원의 도로명 주소',
                         latitude VARCHAR(20) DEFAULT NULL COMMENT '좌표정보(x)',
                         longitude VARCHAR(20) DEFAULT NULL COMMENT '좌표정보(y)',
                         businessStatus ENUM('영업', '폐업') DEFAULT '영업' COMMENT '영업 상태',
                         `type` ENUM('일반', '야간', '24시간') NOT NULL DEFAULT '일반' COMMENT '병원 타입'
);
CREATE TABLE cart(
                     id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                     regDate DATETIME NOT NULL COMMENT '가입 날짜',
                     updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                     memberId INT(10) UNSIGNED NOT NULL COMMENT '생성자 식별번호',
                     itemName TEXT NOT NULL COMMENT '제품이름',
                     itemprice INT(10) UNSIGNED NOT NULL COMMENT '제품가격',
                     itemlink TEXT NOT NULL COMMENT '제품사이트'
);

## GPS 수신 정보 동의 테이블
CREATE TABLE gpsChack(
                         id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                         memberId INT(10) UNSIGNED NOT NULL COMMENT '접속자 식별번호',
                         regDate DATETIME NOT NULL COMMENT '등록 날짜',
                         updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                         latitude DECIMAL(10, 7) COMMENT '현재 위도',
                         longitude DECIMAL(10, 7) COMMENT '현재 경도',
                         location VARCHAR(100) COMMENT '장소'
);

## 산책 테이블
CREATE TABLE walk(
                     id INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '식별번호',
                     memberId INT(10) UNSIGNED NOT NULL COMMENT '접속자 식별번호',
                     updateDate DATETIME NOT NULL COMMENT '수정 날짜',
                     routeName TEXT NOT NULL COMMENT '산책이름',
                     purchaseDate DATE NOT NULL COMMENT '산책 날짜',
                     routePicture TEXT NOT NULL COMMENT '루트 사진',
                     routedistance DOUBLE NOT NULL COMMENT '루트 거리',
                     isLiked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '즐겨찾기 여부 (0=즐겨찾기 전, 1=즐겨찾기 후)'
);

CREATE TABLE webpush (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         memberId INT NOT NULL,
                         `endpoint` VARCHAR(255) NOT NULL,
                         p256dh VARCHAR(255) NOT NULL,
                         auth VARCHAR(255) NOT NULL,
                         createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);