# 코드 정리 목록

volume-4 도메인 레이어 코드 리뷰 결과, 조치가 필요한 항목을 정리한다.

## 완료

- [x] Repository 메서드명에 soft delete 필터링 의도 반영 (`findByIdAndDeletedAtIsNull` 등)

## 아키텍처

- [ ] **User 엔티티의 Spring Security 의존 제거**
  - 파일: `domain/user/User.java` L5
  - `org.springframework.security.crypto.password.PasswordEncoder`를 도메인이 직접 import
  - 조치: `domain/user`에 `PasswordEncryptor` 인터페이스 정의 → `infrastructure/user`에서 Spring Security 위임 구현체 제공

## 도메인 설계

- [ ] **User 엔티티의 IllegalArgumentException을 CoreException으로 통일**
  - 파일: `domain/user/User.java` L50-51, L54, L64, L74
  - 프로젝트 에러 핸들링 규칙(`CoreException` + `ErrorType`)과 불일치
  - `ApiControllerAdvice`에서 별도 처리되지 않으면 500 에러로 응답될 수 있음

- [ ] **UserCoupon.validateUsable(), use()에서 검증 중 상태 변경 부수효과**
  - 파일: `domain/coupon/UserCoupon.java` L70-71, L88-89
  - validate 메서드 내부에서 `expire()`를 호출하여 상태를 변경함
  - 검증과 상태 변경 책임 분리 검토

- [ ] **UserCoupon.restore()의 만료 쿠폰 복원 가능성**
  - 파일: `domain/coupon/UserCoupon.java` L102-104
  - 만료 여부를 확인하지 않고 무조건 AVAILABLE로 변경
  - 주문 취소 시 이미 만료된 쿠폰도 사용 가능 상태로 복원될 수 있음

- [ ] **Product 엔티티 검증 로직 중복 → guard() 활용 검토**
  - 파일: `domain/product/Product.java` L32-37, L45-50
  - 생성자와 `update()`에 동일한 price/stock 검증이 중복
  - `guard()` 오버라이드로 PrePersist/PreUpdate 시점 자동 검증 가능

- [ ] **Brand 엔티티 name 검증 부재**
  - 파일: `domain/brand/Brand.java` L22-24, L26-28
  - null/blank 검증 없이 DB `nullable = false`에만 의존

- [ ] **Order 생성자 불변 조건 부재**
  - 파일: `domain/order/Order.java` L42-49
  - userId null 검증, 빈 주문 생성 방지 등 도메인 자체 방어 부족

- [ ] **BrandService.findBrand() 역할 및 반환 타입 재검토**
  - 파일: `application/brand/BrandService.java` L29
  - 호출부: `ProductFacade` L19, L25 / `BrandFacade` L22
  - 호출하는 쪽에서 반환값(`Brand`)을 사용하지 않고 존재 여부 확인 용도로만 호출
  - 컨벤션 상 Service 메서드 반환은 application DTO여야 하나, 도메인 엔티티를 직접 반환
  - 검토 방향: `getBrand()`로 통일하거나, 존재 확인 전용 `void` 메서드로 분리

## 잠재적 이슈

- [ ] **Coupon, UserCoupon의 LocalDate.now() 직접 호출**
  - 파일: `domain/coupon/Coupon.java` L80, L101 / `domain/coupon/UserCoupon.java` L107
  - 테스트 시 시간 제어 어려움
  - 만료일이 "오늘"인 경우 `isBefore(now)`에서 유효로 판정 — 정책 의도 확인 필요

- [ ] **UserCoupon.calculateDiscount() 정수 연산 버림**
  - 파일: `domain/coupon/UserCoupon.java` L82
  - `totalAmount * discountValue / 100`은 정수 나눗셈으로 소수점 이하 버림 발생
  - 의도된 정책인지 확인 필요

- [ ] **Order.addOrderItem()과 applyDiscount() 호출 순서 의존성**
  - 파일: `domain/order/Order.java` L51-56, L58-61
  - applyDiscount() 이후 addOrderItem()을 호출하면 finalAmount 계산이 부정확해짐
  - 순서 의존적인 로직이 암묵적으로 존재

- [ ] **Order.cancel() 중복 취소 방어 부재**
  - 파일: `domain/order/Order.java` L63-65
  - 서비스에서 `isCancelled()` 체크 후 호출하지만, 도메인 자체 방어 없음
