# 컨벤션

## 1. 아키텍처 원칙
- 레이어드 아키텍처 + DIP(의존성 역전 원칙) 준수
- 도메인 모델 중심 설계 — 비즈니스 규칙은 도메인 계층에 위치
- 외부 기술 의존성은 infrastructure 계층으로 격리

```
  interfaces → application → domain ← infrastructure
                                (DIP: 도메인이 정의한 인터페이스를 infrastructure가 구현)
```
- 역방향 의존 금지 (domain이 상위 계층에 의존하면 안 됨)

## 2. 패키지 구조
4개 레이어 × 도메인별 하위 패키징

| 레이어 | 패키지 | 책임 |
|--------|--------|------|
| presentation | `/interfaces/api` | 요청 수신 및 응답 반환 (비즈니스 로직 금지) |
| application | `/application/..` | 유스케이스 흐름 처리 |
| domain | `/domain/..` | 비즈니스 규칙 및 비즈니스에 필요한 추상화 정의 |
| infrastructure | `/infrastructure/..` | 도메인이 정의한 추상화를 외부 기술로 구현 |

횡단 관심사:
- `support/` — 에러(`CoreException`, `ErrorType`), 인증(`AuthConstants`, `@LoginUser`, `@AdminOnly`)
- `config/` — Spring 설정(`WebMvcConfig` 등)

## 3. Service vs Facade
| 구분 | 책임 | 의존 대상 | 예시 |
|------|------|-----------|------|
| **Service** | 단일 도메인의 유즈케이스 담당 | 자신의 도메인 Repository | `ProductService`, `BrandService` |
| **Facade** | 여러 Service 간의 조합 담당 | 2개 이상의 Service | `ProductFacade`, `OrderFacade` |

- Service는 다른 도메인의 Service나 Repository에 직접 의존하지 않는다.
- 여러 도메인이 엮이는 작업은 반드시 Facade(`@Component`)를 통해 조합한다.
- Controller는 단일 도메인 작업이면 Service를, 여러 도메인이 엮이면 Facade를 호출한다.

## 4. 계층별 DTO
- 각 계층은 자신만의 데이터 객체를 정의하고, 변환 책임은 상위 계층(호출하는 쪽)이 갖는다.
- 매개변수가 3개 이상인 경우 DTO를 생성하여 전달한다.
- DTO는 record를 사용하여 불변성을 유지한다.

| 계층           | 입력      | 출력       | 네이밍 패턴 | 예시 |
|----------------|-----------|------------|-------------|------|
| interface      | `Request` | `Response` | `{Domain}{Action}Request/Response` | `BrandCreateRequest`, `BrandCreateResponse` |
| application    | `Command` | `Result`   | `{Domain}{Action}Command/Result` | `BrandCreateCommand`, `BrandCreateResult` |
| infrastructure | `Dto`     | `Domain`   | - | - |

### 조회 DTO 네이밍
조회 관련 DTO는 행위(`Get`) 대신 용도를 드러내는 이름을 사용한다.

| 구분 | Presentation (Client ↔ Ctrl) | Application (Ctrl ↔ Service) |
|------|------------------------------|------------------------------|
| 단건 상세 조회 | `{Domain}DetailResponse` | `{Domain}Result` |
| 목록/검색 조회 | `{Domain}ListResponse` | `{Domain}Result` |

- 단건과 목록이 동일한 필드를 사용하는 경우 `DetailResponse` 하나로 유지하고, 실제로 분화가 필요한 시점에 `ListResponse`로 분리한다.

- 변환 메서드는 수신 객체에 `static from()` 또는 `toXxx()`로 정의한다.

## 5. 도메인 설계 원칙
- 엔티티 객체는 자기 자신이 유효한 상태임을 보장해야 한다.
- 도메인 객체는 비즈니스 규칙을 캡슐화해야 한다.
- 도메인 서비스는 서로 다른 도메인을 조립해, 도메인 로직을 조정하여 기능을 제공해야 한다.
- 규칙이 여러 서비스에 나타나면 도메인 객체에 속할 가능성이 높다.
- 각 기능에 대한 책임과 결합도에 대해 개발자의 의도를 확인하고 개발을 진행한다.

### BaseEntity 상속
- 모든 엔티티는 `BaseEntity`를 상속 — `id`, `createdAt`, `updatedAt`, `deletedAt` 자동 관리
- Soft Delete: `delete()`/`restore()` 멱등 동작
- Hard Delete 정책이 적용된 도메인도 BaseEntity를 상속한다.(코드 일관성)
- `guard()` 오버라이드로 `@PrePersist`/`@PreUpdate` 시점 검증 가능

### 엔티티 유효성 검증
엔티티의 검증은 **데이터 유효성**과 **비즈니스 규칙**으로 구분한다.

| 구분 | 의미 | 위치 | 예시 |
|------|------|------|------|
| **데이터 유효성** | 엔티티가 존재할 수 있는 상태인가 | `guard()` | name이 null이면 안 된다, price가 음수면 안 된다 |
| **비즈니스 규칙** | 이 행위를 지금 수행할 수 있는가 | 행위 메서드 내부 | 만료된 쿠폰은 사용할 수 없다, 비밀번호는 8자 이상이어야 한다 |

- **데이터 유효성**: `guard()`를 오버라이드하여 한 곳에서 검증한다.
  - 생성자/변경 메서드 끝에서 `guard()`를 명시적으로 호출한다.
  - `@PrePersist`/`@PreUpdate` 시점에도 자동 호출되어 이중 안전망 역할을 한다.
- **비즈니스 규칙**: 해당 행위 메서드 내부에서 인라인으로 검증한다.

### 엔티티 생성 패턴
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Builder
    private Product(/* 필드 */) {
        this.field = field;
        guard(); // 데이터 유효성 검증
    }

    public void changeInfo(/* 필드 */) {
        this.field = field;
        guard(); // 데이터 유효성 검증
    }

    public void use() {
        // 비즈니스 규칙 검증
        if (status == USED) { throw new CoreException(...); }
        this.status = USED;
    }

    @Override
    protected void guard() {
        // 엔티티가 존재할 수 있는 상태인지 검증
        if (field == null) { throw new CoreException(...); }
    }
}
```
- `@Builder` + private 생성자, `@NoArgsConstructor(access = PROTECTED)`
- `@Getter`만 사용, setter 금지 — 변경은 도메인 메서드를 통해서만
- 도메인 모델의 메서드 명칭은 유비쿼터스 언어를 바탕으로 비즈니스 의미가 드러나도록 작성한다.

## 6. 네이밍 규칙

### 클래스 네이밍
| 역할 | 패턴 | 예시 |
|------|------|------|
| Entity | `{Domain}` | `Brand`, `Product` |
| Service | `{Domain}Service` | `BrandService` |
| Facade | `{Domain}Facade` | `ProductFacade` |
| Controller | `{Domain}V1Controller` | `BrandV1Controller` |
| Admin Controller | `{Domain}AdminV1Controller` | `BrandAdminV1Controller` |
| Repository (domain) | `{Domain}Repository` | `BrandRepository` |
| Repository (infra) | `{Domain}RepositoryImpl` | `BrandRepositoryImpl` |
| JPA Repository | `{Domain}JpaRepository` | `BrandJpaRepository` |

### Service 메서드
- 메서드명은 유비쿼터스 언어를 기반으로 비즈니스 의미가 드러나도록 작성한다.
- 유비쿼터스 언어가 정의되어 있지 않으면 정의하고 반영한다.

| 구분 | 네이밍 원칙 | 예시 |
|------|-------------|------|
| 조회 | `get~` — 단건/목록 모두 동일 | `getBrand(Long)`, `getBrands(Pageable)` |
| 변경 행위 | 유비쿼터스 언어(glossary) 기반 동사 | `registerBrand(...)`, `modifyBrand(...)`, `deleteBrand(...)`, `placeOrder(...)`, `cancelOrder(...)` |

### Repository 인터페이스
- soft delete 필터링은 비즈니스 정책이므로, 인터페이스 메서드명에 의도를 명확히 드러낸다.

| 레이어 | 메서드명 | 예시 |
|--------|----------|------|
| **domain** (interface) | 비즈니스 정책이 드러나는 네이밍 | `findAllByDeletedAtIsNull(Pageable)`, `findByIdAndDeletedAtIsNull(Long)` |
| **infrastructure** (구현체) | 인터페이스 구현 | `jpaRepository.findAllByDeletedAtIsNull(pageable)` |

## 7. API 규칙
- URL: 일반 `/api/v1/{resources}`, 관리자 `/api-admin/v1/{resources}` (복수형, kebab-case)
- 공통 응답: `ApiResponse<T>` — `meta(result, errorCode, message)` + `data`
- 날짜 형식: `yyyy-MM-dd` (ISO 8601)
- 페이징 기본값: `page=0`, `size=20`
- 인증: `X-Loopers-LoginId` / `X-Loopers-LoginPw` 헤더 → `@LoginUser AuthUser` 파라미터로 주입
- 관리자: `/api-admin/**` + `@AdminOnly` 어노테이션, `X-Loopers-Ldap`

## 8. 에러 핸들링
- `CoreException(ErrorType)` 또는 `CoreException(ErrorType, customMessage)`으로 예외 발생
- `ErrorType` enum: HTTP 상태 코드(`HttpStatus`) + 에러 코드(`getReasonPhrase()`) + 기본 메시지
- 비즈니스 예외는 도메인/서비스에서 `CoreException`으로 던짐
- `ApiControllerAdvice`에서 전역 처리:
  - `CoreException` → `log.warn`, 해당 HTTP 상태 반환
  - `MethodArgumentNotValidException` 등 검증 예외 → `BAD_REQUEST`
  - `Throwable` → `log.error`, `INTERNAL_ERROR`

## 9. 트랜잭션 규칙
- Service: 조회 `@Transactional(readOnly = true)`, 변경 `@Transactional`
- Facade: 다중 도메인 조합 시 `@Transactional`
- 도메인 엔티티에는 `@Transactional` 사용 금지

## 10. 테스트
- 테스트 코드 생성 시 test-generate 스킬을 따른다.
- 메서드명: 한국어, 유비쿼터스 언어 기반
- 구조: given-when-then
- 도구: JUnit5, BDDMockito, AssertJ, TestContainers(MySQL 8.0)
