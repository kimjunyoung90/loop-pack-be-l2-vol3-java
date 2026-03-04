# 쿠폰 도메인

## 요구사항
1. 주문 시에 사용자가 소유한 쿠폰을 적용해 할인 받음
2. 쿠폰은 재사용 불가
3. 정액/정률 쿠폰 존재
4. 존재하지 않거나, 사용 불가능한 쿠폰으로 요청 시 주문은 실패

## URI
### 일반 사용자
1. 쿠폰 발급 요청
`POST /api/v1/coupons/{couponId}/issues`
2. 내 쿠폰 목록 조회
`GET /api/v1/coupons/me`
- 쿠폰 목록 조회시는 사용 가능한 쿠폰(`AVAILABLE`) 외에 사용 완료(`USED`), 만료(`EXPIRED`)된 쿠폰도 함께 반환

### 관리자 기능
쿠폰 템플릿 : 고객에게 발행하기 위해 생성된 쿠폰(쿠폰 템플릿 내용을 바탕으로 고객에게 쿠폰이 발행된다.)
1. 쿠폰 템플릿 생성
`POST /api-admin/v1/coupons`
쿠폰은 정액(`FIXED`)과 정률(`RATE`)이 있다.
2. 쿠폰 템플릿 조회
다건 조회 : `GET /api-admin/v1/coupons`
페이징
상세 조회 : `GET /api-admin/v1/coupons/{couponId}`
3. 쿠폰 템플릿 수정
`PUT /api-admin/v1/coupons/{couponId}`
4. 쿠폰 템플릿 삭제
`DELETE /api-admin/v1/coupons/{couponId}`
5. 특정 쿠폰 발급 내역 조회
`GET /api-admin/v1/coupons/{couponId}/issues`
페이징

## 개발 순서
1. 용어 사전 정리
2. 요구사항 정리
3. 시퀀스 다이어그램 생성
4. 클래스 다이어그램 생성
5. erd 생성
6. 테스트 코드 작성
7. 개발
8. 동시성 테스트

