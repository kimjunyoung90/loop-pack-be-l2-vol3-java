---
name: generate-test
description:
  테스트 코드를 레이어별로 체계적으로 생성하는 스킬.
  "테스트 코드 작성", "테스트 생성", "테스트 만들어줘" 등의 요청이 있을 때 이 스킬을 사용한다.
---

# Generate Test Skill

지정된 도메인의 소스 코드를 분석하고, 레이어별 테스트 코드를 체계적으로 생성한다.

```
Step 1. 대상 도메인 소스 코드 분석
Step 2. 레이어별 테스트 케이스 설계
Step 3. 테스트 코드 생성
Step 4. 테스트 실행 및 검증
```

---

## Step 1. 대상 도메인 소스 코드 분석

테스트 대상 도메인의 모든 레이어 소스 코드를 읽고 분석한다.

### 읽어야 할 파일

| 레이어 | 대상 파일 |
|---|---|
| **Domain** | 엔티티, Repository 인터페이스 |
| **Application** | Service, Facade(있을 경우), Command, Info DTO |
| **Interfaces** | Controller, API DTO, ApiSpec |
| **Infrastructure** | Repository 구현체, JPA Repository |
| **Support** | CoreException, ErrorType, ApiResponse, ApiControllerAdvice |

### 분석 항목

- 엔티티의 상태 변경 메서드 (update, delete 등)
- 서비스의 비즈니스 로직과 예외 발생 조건
- Facade의 도메인 간 조합 로직
- 컨트롤러의 엔드포인트, 인증/인가 처리
- 기존 테스트 파일이 있으면 함께 읽어 컨벤션 파악

---

## Step 2. 레이어별 테스트 케이스 설계

소스 코드 분석을 바탕으로, 레이어별로 테스트 케이스를 설계한다.
**테스트 코드를 작성하기 전에, 설계 결과를 사용자에게 보여주고 확인을 받는다.**

### 테스트 종류 및 범위

| 테스트 종류 | 파일 위치 | 범위 |
|---|---|---|
| **Domain 단위 테스트** | `test/.../domain/{도메인}/` | 엔티티의 상태 변경 메서드, 도메인 규칙 |
| **Service 단위 테스트** | `test/.../application/{도메인}/` | Mock 기반 서비스 로직 검증 |
| **Facade 단위 테스트** | `test/.../application/{도메인}/` | Mock 기반 도메인 간 조합 로직 검증 (Facade가 있을 때만) |
| **Controller 슬라이스 테스트** | `test/.../interfaces/api/{도메인}/` | @WebMvcTest 기반 API 동작 검증 |
| **통합 테스트** | `test/.../application/{도메인}/` | @SpringBootTest + Testcontainers 전체 흐름 검증 |

### 테스트 케이스 설계 출력 형식

레이어별로 테스트 메서드명을 나열한다.

```
### Domain 단위 테스트: {Entity}Test

- 상품_정보를_변경하면_brandId_name_price_stock이_모두_변경된다
- 상품을_삭제하면_deletedAt이_설정된다

### Service 단위 테스트: {Service}Test

- 상품을_생성하면_저장된_상품의_ProductInfo를_반환한다
- 존재하지_않는_상품을_조회하면_CoreException_NOT_FOUND가_발생한다
...
```

---

## Step 3. 테스트 코드 생성

사용자 확인 후, 설계된 테스트 케이스를 코드로 작성한다.

### 테스트 메서드 네이밍 규칙

**핵심 원칙: "성공한다", "예외가 발생한다" 같은 모호한 표현을 사용하지 않는다.**

테스트 메서드명은 `{조건}_{구체적_결과}` 형태로 작성한다.

#### 성공 케이스 - 구체적 결과를 명시

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `상품을_생성하면_성공한다` | `상품을_생성하면_저장된_상품의_ProductInfo를_반환한다` | 반환값이 무엇인지 명시 |
| `상품을_수정하면_성공한다` | `상품을_수정하면_변경된_정보가_반영된_ProductInfo를_반환한다` | 변경이 반영됨을 명시 |
| `상품을_삭제하면_성공한다` | `상품을_삭제하면_deletedAt이_설정된다` | 상태 변화를 명시 |
| `상품_목록_조회에_성공한다` | `상품_목록을_조회하면_삭제되지_않은_상품을_Page로_반환한다` | 필터링 조건과 반환 타입 명시 |
| `상품_등록에_성공한다` | `상품을_등록하고_200_OK와_생성된_상품_정보를_반환한다` | HTTP 상태 코드와 응답 내용 명시 |

#### 실패 케이스 - 구체적 예외를 명시

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `예외가_발생한다` | `CoreException_NOT_FOUND가_발생한다` | 예외 클래스와 ErrorType 명시 |
| `실패한다` | `403_FORBIDDEN을_반환한다` | HTTP 상태 코드 명시 (컨트롤러) |

### 코딩 컨벤션

| 항목 | 규칙 |
|---|---|
| **구조** | given / when / then (AAA 패턴) |
| **Assertion** | AssertJ (`assertThat`, `assertThatThrownBy`) |
| **Mocking** | BDDMockito (`given`, `willReturn`, `willThrow`, `verify`) |
| **메서드명** | 한글 + 언더스코어 (`void 메서드명()`) |
| **클래스명** | 영문 (`{Domain}Test`, `{Service}Test`, `{Facade}Test`, `{Controller}Test`) |
| **접근 제어자** | 테스트 클래스와 메서드에 접근 제어자 생략 (package-private) |

### 레이어별 테스트 패턴

#### Domain 단위 테스트

```java
class ProductTest {
    @Test
    void 상품_정보를_변경하면_brandId_name_price_stock이_모두_변경된다() {
        // given - Builder로 엔티티 생성
        // when - 상태 변경 메서드 호출
        // then - assertThat으로 변경된 상태 검증
    }
}
```

#### Service 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 상품을_생성하면_저장된_상품의_ProductInfo를_반환한다() {
        // given - Mock 설정 (BDDMockito.given)
        // when - 서비스 메서드 호출
        // then - 반환값 검증
    }
}
```

#### Facade 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {
    @Mock
    private ProductService productService;
    @Mock
    private BrandService brandService;

    @InjectMocks
    private ProductFacade productFacade;

    @Test
    void 존재하는_브랜드로_상품을_생성하면_브랜드_검증_후_ProductInfo를_반환한다() {
        // given
        // when
        // then - verify로 브랜드 검증 호출 확인 + 반환값 검증
    }
}
```

#### Controller 슬라이스 테스트

```java
@WebMvcTest(ProductV1Controller.class)
class ProductV1ControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    @Test
    void 상품_목록을_조회하면_200_OK와_페이징된_상품_목록을_반환한다() throws Exception {
        // given - MockitoBean 설정
        // when & then - mockMvc.perform + andExpect
    }
}
```

- Admin 컨트롤러 테스트에는 인증/인가 실패 케이스(403)를 반드시 포함한다.
- `@MockitoBean`은 `org.springframework.test.context.bean.override.mockito.MockitoBean`을 사용한다.

#### 통합 테스트

```java
@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Test
    void 상품_등록_조회_수정_삭제_전체_흐름을_검증한다() {
        // CRUD 전체 흐름을 하나의 테스트에서 검증
    }
}
```

---

## Step 4. 테스트 실행 및 검증

작성한 테스트를 실행하여 모든 테스트가 통과하는지 확인한다.

```bash
./gradlew :apps:commerce-api:test --tests "{테스트 클래스 패턴}"
```

- 실패하는 테스트가 있으면 원인을 분석하고 수정한다.
- 모든 테스트 통과 후, 결과를 요약하여 사용자에게 보고한다.

### 결과 보고 형식

```
## 테스트 결과 요약

| 테스트 파일 | 유형 | 테스트 수 | 결과 |
|---|---|---|---|
| ProductTest | Domain 단위 | 3개 | ✅ 통과 |
| ProductServiceTest | Service 단위 | 8개 | ✅ 통과 |
| ... | ... | ... | ... |
```

---

## 공통 주의사항

1. 소스 코드를 읽지 않은 상태에서 테스트를 작성하지 않는다.
2. 기존 테스트 파일이 있으면 반드시 읽어 컨벤션을 따른다.
3. 테스트 메서드명에 "성공한다", "실패한다", "예외가 발생한다" 같은 모호한 표현을 사용하지 않는다.
4. 테스트 코드 작성 전 반드시 Step 2의 설계 결과를 사용자에게 확인받는다.
5. 불필요한 테스트(getter 검증, 단순 위임 등)는 작성하지 않는다.
