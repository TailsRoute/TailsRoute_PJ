USE `tails_route`;

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

select * from hospital;