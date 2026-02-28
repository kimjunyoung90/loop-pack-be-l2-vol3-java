## 도메인 & 객체 설계 전략
- 도메인 객체는 비즈니스 규칙을 캡슐화해야 합니다.
- 애플리케이션 서비스는 서로 다른 도메인을 조립해, 도메인 로직을 조정하여 기능을 제공해야 합니다.
- 규칙이 여러 서비스에 나타나면 도메인 객체에 속할 가능성이 높습니다.
- 각 기능에 대한 책임과 결합도에 대해 개발자의 의도를 확인하고 개발을 진행합니다.

## 코드 작성 컨벤션

### 아키텍처 및 패키지 구성
- 본 프로젝트는 레이어드 아키텍처를 따르며, DIP (의존성 역전 원칙) 을 준수합니다.
- API request, response DTO와 응용 레이어의 DTO는 분리해 작성하도록 합니다.
- 패키징 전략은 4개 레이어 패키지를 두고, 하위에 도메인별로 패키징하는 형태로 작성한다.
- 각 레이어의 역할은 아래와 같다.

| 레이어 | 역할 |
|--------|------|
| **interfaces/api** | REST API 엔드포인트 정의, 요청/응답 DTO 및 변환 |
| **application** | 도메인 조합을 통한 유즈케이스 처리, 서비스 입출력 DTO |
| **domain** | 엔티티, VO, Repository 인터페이스 — 비즈니스 규칙 캡슐화 |
| **infrastructure** | Repository 구현체 — JPA, soft delete 등 인프라 세부사항 캡슐화 |

#### 계층별 DTO 전략
- 각 계층은 자신만의 데이터 객체를 정의하고, 변환 책임은 상위 계층(호출하는 쪽)이 갖는다.
- 의존 방향: interfaces → application → domain (역방향 의존 금지)

| 계층 | 입력 | 출력 | 변환 위치 |
|------|------|------|-----------|
| interfaces | `V1Dto.XxxRequest` | `V1Dto.XxxResponse` | Controller (`Request→Command`, `Info→Response`) |
| application | `XxxCommand` | `XxxInfo` | Service (`Entity→Info`) |
| domain | 원시 타입 / VO | 엔티티 | - |

### Application 레이어 책임 분리: Service vs Facade
| 구분 | 책임 | 의존 대상 | 예시 |
|------|------|-----------|------|
| **Service** | 단일 도메인의 유즈케이스 담당 | 자신의 도메인 Repository | `ProductService`, `BrandService` |
| **Facade** | 여러 Service 간의 조합 담당 | 2개 이상의 Service | `ProductFacade`, `BrandFacade` |

- Service는 다른 도메인의 Service나 Repository에 직접 의존하지 않는다.
- 여러 도메인이 엮이는 작업은 반드시 Facade를 통해 조합한다.
- Controller는 단일 도메인 작업이면 Service를, 여러 도메인이 엮이면 Facade를 호출한다.

### Service 메서드 네이밍
| 접두어 | 용도 | 반환 타입 | 예시 |
|--------|------|-----------|------|
| `get~` | 단건/목록 조회 | application DTO | `getBrand(Long): BrandInfo` |
| `find~` | 엔티티 조회 (Facade 내부용) | 도메인 엔티티 | `findBrand(Long): Brand` |
| `create~` | 생성 | application DTO | `createProduct(Brand, Command): ProductInfo` |
| `update~` | 수정 | application DTO | `updateProduct(Long, Brand, Command): ProductInfo` |
| `delete~` | 삭제 | void | `deleteProduct(Long): void` |

### Repository 인터페이스 메서드 네이밍
- 도메인 레이어의 Repository 인터페이스는 **비즈니스 의미만 표현**한다.
- 인프라 세부사항(`deletedAt`, `IsNull` 등)은 인터페이스에 노출하지 않는다.
- soft delete 필터링은 infrastructure 구현체 내부에서 캡슐화한다.

| 레이어 | 메서드명 | 예시 |
|--------|----------|------|
| **domain** (interface) | 비즈니스 의미만 표현 | `findAll(Pageable)`, `findById(Long)`, `findAllByBrand(Brand)` |
| **infrastructure** (구현체) | 내부에서 soft delete 조건 처리 | `jpaRepository.findAllByDeletedAtIsNull(pageable)` |

### 날짜 포맷
- API 요청/응답의 날짜 형식은 `yyyy-MM-dd` (ISO 8601)를 사용한다.