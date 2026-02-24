## 개념 질문 자동 정리
- 사용자가 작업 중 프로그래밍 개념, 문법, 용어에 대해 질문하면 `concept-to-wil` 스킬을 사용합니다.
- 스킬이 개념 설명 후 옵시디언 WIL 문서에 자동으로 정리합니다.

## 도메인 & 객체 설계 전략
- 도메인 객체는 비즈니스 규칙을 캡슐화해야 합니다.
- 애플리케이션 서비스는 서로 다른 도메인을 조립해, 도메인 로직을 조정하여 기능을 제공해야 합니다.
- 규칙이 여러 서비스에 나타나면 도메인 객체에 속할 가능성이 높습니다.
- 각 기능에 대한 책임과 결합도에 대해 개발자의 의도를 확인하고 개발을 진행합니다.

## 코드 작성 컨벤션

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

## 아키텍처, 패키지 구성 전략
- 본 프로젝트는 레이어드 아키텍처를 따르며, DIP (의존성 역전 원칙) 을 준수합니다.
- API request, response DTO와 응용 레이어의 DTO는 분리해 작성하도록 합니다.
- 패키징 전략은 4개 레이어 패키지를 두고, 하위에 도메인 별로 패키징하는 형태로 작성합니다.
    - 예시
      /interfaces/api (presentation 레이어 - API)
      /application/.. (application 레이어 - 도메인 레이어를 조합해 사용 가능한 기능을 제공)
      /domain/.. (domain 레이어 - 도메인 객체 및 엔티티, Repository 인터페이스가 위치)
      /infrastructure/.. (infrastructure 레이어 - JPA, Redis 등을 활용해 Repository 구현체를 제공)

### 계층별 DTO 전략
- 각 계층은 자신만의 데이터 객체를 정의하고, 변환 책임은 상위 계층(호출하는 쪽)이 갖는다.
- 의존 방향: interfaces → application → domain (역방향 의존 금지)

| 계층 | 입력 | 출력 | 변환 위치 |
|------|------|------|-----------|
| interfaces | `V1Dto.XxxRequest` | `V1Dto.XxxResponse` | Controller (`Request→Command`, `Info→Response`) |
| application | `XxxCommand` | `XxxInfo` | Service (`Entity→Info`) |
| domain | 원시 타입 / VO | 엔티티 | - |