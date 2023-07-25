--1번 매장 추가
INSERT INTO shop(member_id, name, introduce, star, star_sum, review_count, address,
latitude, longitude, res_open_week, res_open_count, created_at, is_deleted)
VALUES(1, '빵집', '빵', 0, 0, 0, '서울', 37.2, 127.2, 1, 5, '2023-07-25', FALSE);

INSERT INTO res_open_day(shop_id, open_day)
VALUES(1, 'MON');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(1, 'TUE');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(1, 'WED');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(1, 'THU');

INSERT INTO res_open_time(shop_id, open_time)
VALUES(1, '09:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(1, '10:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(1, '11:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(1, '11:30:00');

--2번 매장 추가
INSERT INTO shop(member_id, name, introduce, star, star_sum, review_count, address,
latitude, longitude, res_open_week, res_open_count, created_at, is_deleted)
VALUES(2, '미용실', '싹둑', 0, 0, 0, '서울', 37.3, 127.4, 1, 1, '2023-07-25', FALSE);

INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'MON');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'TUE');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'WED');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'THU');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'FRI');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'SAT');
INSERT INTO res_open_day(shop_id, open_day)
VALUES(2, 'SUN');

INSERT INTO res_open_time(shop_id, open_time)
VALUES(2, '09:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(2, '10:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(2, '11:00:00');
INSERT INTO res_open_time(shop_id, open_time)
VALUES(2, '11:30:00');

--3번 매장 추가
INSERT INTO shop(member_id, name, introduce, star, star_sum, review_count, address,
latitude, longitude, res_open_week, res_open_count, created_at, is_deleted)
VALUES(2, 'XX', 'XX', 0, 0, 0, 'XX', 37.3, 127.4, 1, 1, '2023-07-25', TRUE);