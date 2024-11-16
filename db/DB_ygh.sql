## 게시글 랜덤 테스트 데이터
INSERT INTO article SET
    regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
memberId = FLOOR(1 + RAND() * 6),
boardId = FLOOR(1 + RAND() * 5),
title = CONCAT('제목', FLOOR(RAND() * 10000)),
`body` = CONCAT('내용', FLOOR(RAND() * 10000));

## 댓글 랜덤 테스트 데이터
INSERT INTO reply SET
    regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
memberId = FLOOR(1 + RAND() * 6),
relTypeCode = 'article',
relId = FLOOR(1 + RAND() * 10),
`body` = CONCAT('내용', FLOOR(RAND() * 10000));