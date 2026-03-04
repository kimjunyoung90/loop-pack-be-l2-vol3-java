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
각 계층은 자신만의 데이터 객체를 정의하고, 변환 책임은 상위 계층(호출하는 쪽)이 갖는다.

| 계층 | 입력 | 출력 | 변환 위치 |
|------|------|------|-----------|
| interfaces | `V1Dto.XxxRequest` | `V1Dto.XxxResponse` | Controller (`Request→Command`, `Info→Response`) |
| application | `XxxCommand` | `XxxInfo` | Service (`Entity→Info`) |
| domain | 원시 타입 / VO | 엔티티 | - |

- **API DTO**: `{Domain}V1Dto` 클래스 내부에 record로 그룹화 (Request/Response)
- **Application DTO**: 별도 파일 (`{Domain}Info.java`, `Create{Domain}Command.java`)
- **변환**: `static from()` 메서드 사용
- **Controller 흐름**: Request 검증(`@Valid`) → Command 변환 → Service/Facade 호출 → Info → Response 변환 → `ApiResponse.success()` 반환

## 5. 도메인 설계 원칙
- 도메인 객체는 비즈니스 규칙을 캡슐화해야 한다.
- 애플리케이션 서비스는 서로 다른 도메인을 조립해, 도메인 로직을 조정하여 기능을 제공해야 한다.
- 규칙이 여러 서비스에 나타나면 도메인 객체에 속할 가능성이 높다.
- 각 기능에 대한 책임과 결합도에 대해 개발자의 의도를 확인하고 개발을 진행한다.

### BaseEntity 상속
- 모든 엔티티는 `BaseEntity`를 상속 — `id`, `createdAt`, `updatedAt`, `deletedAt` 자동 관리
- Soft Delete: `delete()`/`restore()` 멱등 동작
- `guard()` 오버라이드로 `@PrePersist`/`@PreUpdate` 시점 검증 가능

### 엔티티 생성 패턴
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Builder
    private Product(/* 필드 */) {
        // 비즈니스 규칙 검증
        this.field = field;
    }

    public void update(/* 필드 */) {
        // 비즈니스 규칙 검증
    }
}
```
- `@Builder` + private 생성자, `@NoArgsConstructor(access = PROTECTED)`
- `@Getter`만 사용, setter 금지 — 변경은 도메인 메서드를 통해서만
- 비즈니스 규칙 검증은 생성자/변경 메서드에서 수행

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
| 접두어 | 용도 | 반환 타입 | 예시 |
|--------|------|-----------|------|
| `get~` | 단건/목록 조회 | application DTO | `getBrand(Long): BrandInfo` |
| `create~` | 생성 | application DTO | `createProduct(Brand, Command): ProductInfo` |
| `update~` | 수정 | application DTO | `updateProduct(Long, Brand, Command): ProductInfo` |
| `delete~` | 삭제 | void | `deleteProduct(Long): void` |

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
- 관리자: `/api-admin/**` + `@AdminOnly` 어노테이션, `X-Loopers-Ldap` 헤더(`loopers.admin`)

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

### 테스트 유형
| 유형 | 클래스 접미사 | 어노테이션 | 목적 |
|------|-------------|-----------|------|
| Domain/Unit | `*Test` | `@ExtendWith(MockitoExtension.class)` | 엔티티·서비스 단위 테스트 (Mockito) |
| Integration | `*IntegrationTest` | `@SpringBootTest` + `@Import(MySqlTestContainersConfig.class)` + `@Transactional` | 계층 통합 검증 |
| Controller | `*ControllerTest` | `@WebMvcTest` + `@Import({LoginUserArgumentResolver.class, AdminAuthInterceptor.class})` | API 계층 슬라이스 테스트 |
| E2E | `*E2ETest` | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Import(MySqlTestContainersConfig.class)` | 실제 HTTP 요청 전체 흐름 검증 |

### 테스트 규칙
- 메서드명: 한국어로 행위 기술 (`유효한_이름으로_브랜드를_생성하면_성공한다`)
- 구조: given-when-then
- 도구: JUnit5, Mockito(BDDMockito: `given`/`willReturn`), AssertJ(`assertThat`), TestContainers(MySQL 8.0)
