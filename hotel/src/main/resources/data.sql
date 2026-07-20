INSERT INTO hotel_type (title, created_at, updated_at, deleted)
SELECT '호텔', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_type WHERE title = '호텔');

INSERT INTO hotel_type (title, created_at, updated_at, deleted)
SELECT '리조트', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_type WHERE title = '리조트');

INSERT INTO hotel_type (title, created_at, updated_at, deleted)
SELECT '펜션/풀빌라', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_type WHERE title = '펜션/풀빌라');

INSERT INTO room_type (title, created_at, updated_at, deleted)
SELECT '스탠다드 룸', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE title = '스탠다드 룸');

INSERT INTO room_type (title, created_at, updated_at, deleted)
SELECT '디럭스 룸', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE title = '디럭스 룸');

INSERT INTO room_type (title, created_at, updated_at, deleted)
SELECT '스위트 룸', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE title = '스위트 룸');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '무료 와이파이', '전 객실 이용 가능', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '무료 와이파이');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '무료 주차', '투숙객 무료 주차', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '무료 주차');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '조식 포함', '일부 객실 조식 포함', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '조식 포함');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '수영장', '공용 수영장 운영', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '수영장');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '피트니스', '피트니스 센터 운영', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '피트니스');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '무료 취소', '일부 요금제 무료 취소', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '무료 취소');

INSERT INTO hotel_amenities (title, description, created_at, updated_at, deleted)
SELECT '반려동물', '일부 객실 동반 가능', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel_amenities WHERE title = '반려동물');

INSERT INTO room_tag (title, created_at, updated_at, deleted)
SELECT '도심뷰', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_tag WHERE title = '도심뷰');

INSERT INTO room_tag (title, created_at, updated_at, deleted)
SELECT '오션뷰', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_tag WHERE title = '오션뷰');

INSERT INTO room_tag (title, created_at, updated_at, deleted)
SELECT '조식포함', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_tag WHERE title = '조식포함');

INSERT INTO room_tag (title, created_at, updated_at, deleted)
SELECT '무료취소', NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM room_tag WHERE title = '무료취소');

INSERT INTO terms (term_type, title, version, is_required, effective_at, created_at, updated_at, deleted)
SELECT 'SERVICE', '이용약관 동의', '1.0', true, NOW(), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_type = 'SERVICE' AND deleted = false);

INSERT INTO terms (term_type, title, version, is_required, effective_at, created_at, updated_at, deleted)
SELECT 'PRIVACY', '개인정보 수집 및 이용 동의', '1.0', true, NOW(), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_type = 'PRIVACY' AND deleted = false);

INSERT INTO terms (term_type, title, version, is_required, effective_at, created_at, updated_at, deleted)
SELECT 'MARKETING', '마케팅 정보 수신 동의', '1.0', false, NOW(), NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_type = 'MARKETING' AND deleted = false);

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '호텔' LIMIT 1), '그랜드 서울 호텔', 240000, '서울특별시 중구 세종대로 110', 5, '서울 도심 중심에 위치한 5성급 호텔입니다.', 37.5665, 126.9780, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '그랜드 서울 호텔');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '호텔' LIMIT 1), '골든 서울 호텔', 180000, '서울 강서구 염창동 공항대로 663', 4, '한강과 가까운 위치의 깔끔한 비즈니스 호텔입니다.', 37.5481, 126.8754, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '골든 서울 호텔');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '호텔' LIMIT 1), '서울 골든 호텔', 165000, '서울 강서구', 4, '강서구 주요 교통지와 가까운 실속형 호텔입니다.', 37.5509, 126.8495, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '서울 골든 호텔');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '리조트' LIMIT 1), '리버사이드 리조트', 220000, '경기도 가평군 청평면 북한강로 120', 4, '강변 전망과 여유로운 휴식을 즐길 수 있는 리조트입니다.', 37.7351, 127.4142, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '리버사이드 리조트');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '펜션/풀빌라' LIMIT 1), '지구 풀빌라 스테이', 310000, '지구', 5, '프라이빗 수영장과 감성 인테리어를 갖춘 풀빌라입니다.', 35.1796, 129.0756, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '지구 풀빌라 스테이');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '펜션/풀빌라' LIMIT 1), '지구 감성 펜션', 145000, '지구', 3, '커플과 가족 여행객에게 어울리는 조용한 펜션입니다.', 35.1803, 129.0771, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '지구 감성 펜션');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '호텔' LIMIT 1), '커플 시그니처 호텔', 210000, '서울특별시 마포구 양화로 45', 4, '커플 여행과 기념일 숙박에 어울리는 감성 호텔입니다.', 37.5563, 126.9236, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '커플 시그니처 호텔');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '리조트' LIMIT 1), '오션뷰 블루 리조트', 280000, '부산광역시 해운대구 해운대해변로 264', 5, '해운대 바다 전망을 즐길 수 있는 프리미엄 리조트입니다.', 35.1587, 129.1604, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '오션뷰 블루 리조트');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '호텔' LIMIT 1), '제주 시티 호텔', 130000, '제주특별자치도 제주시 연동 261', 3, '공항과 가까운 위치의 합리적인 가격대 호텔입니다.', 33.4890, 126.4983, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '제주 시티 호텔');

INSERT INTO hotel (type_id, hotel_name, hotel_price, location, star_rating, description, latitude, longitude, created_at, updated_at, deleted)
SELECT (SELECT sid FROM hotel_type WHERE title = '리조트' LIMIT 1), '숲속 글램핑 하우스', 175000, '강원특별자치도 평창군 봉평면 태기로 174', 4, '자연 속에서 편안한 글램핑과 숙박을 함께 즐길 수 있습니다.', 37.6173, 128.3795, NOW(), NOW(), false
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE hotel_name = '숲속 글램핑 하우스');

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '스탠다드 더블룸', 180000, true, 201, 4, 28, 2, 15, 11, '가능', 'BAN', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '골든 서울 호텔' AND rt.title = '스탠다드 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 201);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '디럭스 더블룸', 240000, true, 101, 5, 32, 2, 15, 11, '가능', 'BAN', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '그랜드 서울 호텔' AND rt.title = '디럭스 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 101);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '프리미어 스위트', 360000, true, 102, 12, 58, 4, 15, 11, '가능', 'LIMITED', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '그랜드 서울 호텔' AND rt.title = '스위트 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 102);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '비즈니스 더블룸', 165000, true, 301, 3, 26, 2, 15, 11, '가능', 'BAN', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '서울 골든 호텔' AND rt.title = '스탠다드 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 301);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '리버뷰 디럭스룸', 220000, true, 401, 2, 34, 2, 15, 11, '가능', 'LIMITED', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '리버사이드 리조트' AND rt.title = '디럭스 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 401);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '프라이빗 풀빌라', 310000, true, 501, 1, 72, 4, 15, 11, '가능', 'POSSIBLE', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '지구 풀빌라 스테이' AND rt.title = '스위트 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 501);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '감성 커플룸', 145000, true, 601, 2, 30, 2, 15, 11, '가능', 'LIMITED', 'BAN', 'OPTIONAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '지구 감성 펜션' AND rt.title = '스탠다드 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 601);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '커플 시그니처룸', 210000, true, 701, 8, 34, 2, 15, 11, '가능', 'BAN', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '커플 시그니처 호텔' AND rt.title = '디럭스 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 701);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '오션뷰 디럭스룸', 280000, true, 801, 15, 38, 2, 15, 11, '가능', 'BAN', 'BAN', 'ESSENTIAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '오션뷰 블루 리조트' AND rt.title = '디럭스 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 801);

INSERT INTO room (hotel_id, room_type_id, room_name, room_price, room_available, room_number, floor, area, maximum_people, check_in_time, check_out_time, parking, pet, smoke, id_card, created_at, updated_at, deleted)
SELECT h.sid, rt.sid, '시티 스탠다드룸', 130000, true, 901, 5, 24, 2, 15, 11, '가능', 'BAN', 'BAN', 'OPTIONAL', NOW(), NOW(), false
FROM hotel h, room_type rt
WHERE h.hotel_name = '제주 시티 호텔' AND rt.title = '스탠다드 룸'
AND NOT EXISTS (SELECT 1 FROM room r WHERE r.hotel_id = h.sid AND r.room_number = 901);

INSERT INTO promotion (type_id, promotion_name, condition_type, start_date, end_date, discount_info, created_at, updated_at, deleted)
SELECT rt.sid, '오픈 기념 15% 할인', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY), '15%', NOW(), NOW(), false
FROM room_type rt
WHERE rt.title = '디럭스 룸'
AND NOT EXISTS (SELECT 1 FROM promotion WHERE promotion_name = '오픈 기념 15% 할인' AND deleted = false);

INSERT INTO promotion (type_id, promotion_name, condition_type, start_date, end_date, discount_info, created_at, updated_at, deleted)
SELECT rt.sid, '스위트 특가 30000원 할인', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 45 DAY), '30,000원', NOW(), NOW(), false
FROM room_type rt
WHERE rt.title = '스위트 룸'
AND NOT EXISTS (SELECT 1 FROM promotion WHERE promotion_name = '스위트 특가 30000원 할인' AND deleted = false);

INSERT INTO coupon (coupon_name, discount_type, discount_value, min_order_amount, start_date, end_date, total_quantity)
SELECT '신규가입 10000원 할인 쿠폰', 'FIXED', 10000, 50000, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 1000
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE coupon_name = '신규가입 10000원 할인 쿠폰');

INSERT INTO coupon (coupon_name, discount_type, discount_value, min_order_amount, start_date, end_date, total_quantity)
SELECT '여름 특가 10% 쿠폰', 'PERCENT', 10, 100000, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 500
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE coupon_name = '여름 특가 10% 쿠폰');
