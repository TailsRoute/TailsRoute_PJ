INSERT INTO article SET
    regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
memberId = FLOOR(1 + RAND() * 6),
boardId = FLOOR(1 + RAND() * 3),
title = CONCAT('제목', FLOOR(RAND() * 10000)),
`body` = CONCAT('내용', FLOOR(RAND() * 10000)),
hitCount = FLOOR(1 + RAND() * 100),
goodReactionPoint = FLOOR(1 + RAND() * 100);

INSERT INTO reply SET
    regDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
memberId = FLOOR(1 + RAND() * 6),
relTypeCode = 'article',
relId = FLOOR(1 + RAND() * 10),
`body` = CONCAT('내용', FLOOR(RAND() * 10000));

INSERT INTO walk SET
    memberId = FLOOR(1 + RAND() * 6),
updateDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
routeName = CONCAT('제목', FLOOR(RAND() * 10000)),
purchaseDate = DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 864000) - 864000) SECOND),
routePicture = CONCAT('경로', FLOOR(RAND() * 10000)),
routedistance = ROUND(RAND() * 9.9 + 0.1, 1);