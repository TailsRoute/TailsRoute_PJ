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

## 24시간 병원 테이블 (크롤링 결과)
CREATE TABLE `temp_hospital` (
                                 `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 병원 ID',
                                 `name` TEXT NOT NULL COMMENT '병원 이름',
                                 `callNumber` VARCHAR(20) UNIQUE DEFAULT NULL COMMENT '소재지전화번호',
                                 `jibunAddress` TEXT DEFAULT NULL COMMENT '병원의 지번 주소',
                                 `roadAddress` TEXT DEFAULT NULL COMMENT '병원의 도로명 주소',
                                 `type` ENUM('일반','24시간') NOT NULL DEFAULT '일반' COMMENT '병원 타입',
                                 PRIMARY KEY (`id`)
);

SELECT COUNT(*) FROM hospital;
SELECT COUNT(*) FROM temp_hospital;

SELECT * FROM temp_hospital;
SELECT * FROM hospital;



SELECT COUNT(*)
FROM hospital h
         INNER JOIN temp_hospital th
                    ON h.callNumber = th.callNumber;

SELECT COUNT(*)
FROM hospital h
WHERE callNumber IS NULL;

SELECT COUNT(*)
FROM hospital h
         INNER JOIN temp_hospital th
                    ON h.callNumber = th.callNumber;

-- 24 단어가 들어가는 hospital 데이터
SELECT COUNT(*) FROM hospital WHERE `name` LIKE '%24%';

-- 전화번호는 일치하지만 24 단어가 들어가지 않는 것
SELECT COUNT(*)
FROM hospital h
         INNER JOIN temp_hospital th
                    ON h.callNumber = th.callNumber
WHERE h.name NOT LIKE '%24%';

-- 주소의 일정 부분이 같으면 매칭
SELECT h.name, th.name, h.roadAddress, th.roadAddress
FROM hospital h
         INNER JOIN temp_hospital th
                    ON SUBSTRING_INDEX(h.roadAddress, ' ', 4) = SUBSTRING_INDEX(th.roadAddress, ' ', 4)
WHERE REPLACE(h.callNumber, '-', '') != REPLACE(th.callNumber, '-', '');

SELECT h.name, th.name, h.roadAddress, th.jibunAddress
FROM hospital h
         INNER JOIN temp_hospital th
                    ON SUBSTRING_INDEX(h.roadAddress, ' ', 5) = SUBSTRING_INDEX(th.jibunAddress, ' ', 5)
WHERE REPLACE(h.callNumber, '-', '') != REPLACE(th.callNumber, '-', '');


# 1. 전화번호가 일치하는 데이터 업데이트
UPDATE hospital h
    INNER JOIN temp_hospital th
ON h.callNumber = th.callNumber
    SET h.type = '24시간';

# 2. name에 '24'가 포함된 데이터 업데이트
UPDATE hospital
SET TYPE = '24시간'
WHERE `name` LIKE '%24%';

# 3. 전화번호는 일치하지만 name에 '24'가 포함되지 않는 데이터 업데이트
UPDATE hospital h
    INNER JOIN temp_hospital th
ON h.callNumber = th.callNumber
    SET h.type = '24시간'
WHERE h.name NOT LIKE '%24%';

# 4. 주소의 일정 부분(앞 4개 단어)이 같고 전화번호가 다른 데이터 업데이트
UPDATE hospital h
    INNER JOIN temp_hospital th
ON SUBSTRING_INDEX(h.roadAddress, ' ', 4) = SUBSTRING_INDEX(th.roadAddress, ' ', 4)
    SET h.type = '24시간';


# 5. roadAddress와 jibunAddress의 일정 부분(앞 5개 단어)이 같고 전화번호가 다른 데이터 업데이트
UPDATE hospital h
    INNER JOIN temp_hospital th
ON SUBSTRING_INDEX(h.roadAddress, ' ', 5) = SUBSTRING_INDEX(th.jibunAddress, ' ', 5)
    SET h.type = '24시간';


SELECT * FROM hospital WHERE `type`='24시간';
SELECT COUNT(*) FROM hospital WHERE `type`='24시간';

