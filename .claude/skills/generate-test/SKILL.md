---
name: Generate-Test
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
| **Domain** | 엔티티, repository 인터페이스 |
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
| **Service 단위 테스트** | `test/.../application/{도메인}/` | mock 기반 서비스 로직 검증 |
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

- 존재하지_않는_상품_조회_시_CoreException_NOT_FOUND가_발생한다
- 이미_발급받은_쿠폰을_다시_발급하면_CoreException_CONFLICT가_발생한다

### 통합 테스트: {Service}IntegrationTest

- 상품_등록_조회_수정_삭제_전체_흐름을_검증한다
...
```

---

## Step 3. 테스트 코드 생성

사용자 확인 후, 설계된 테스트 케이스를 코드로 작성한다.

### 테스트 메서드 네이밍 규칙

**핵심 원칙: "성공한다", "예외가 발생한다" 같은 모호한 표현을 사용하지 않는다.**

테스트 메서드명은 `{조건}_{행위}_시_{구체적_기대결과}` 형태로 작성한다.

**메서드명에 구현 메서드명(validateUsable, calculateDiscount 등)을 넣지 않는다.**
구현 메서드명 대신 해당 메서드가 수행하는 **비즈니스 행위**로 표현한다.
구현 메서드명이 바뀌면 테스트명도 바꿔야 하므로, 행위 기반으로 작성해야 리팩토링에 강하다.

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `validateUsable에서_BAD_Request가_발생한다` | `사용_가능_여부_검증_시_BAD_REQUEST가_발생한다` | 비즈니스 행위로 표현 |
| `calculateDiscount가_5000을_반환한다` | `정액_할인_쿠폰은_고정_금액을_할인한다` | 할인 계산의 비즈니스 의미 표현 |
| `validateUsable이_정상_통과한다` | `사용_가능_여부_검증_시_예외가_발생하지_않는다` | 구현명 제거 + 결과 명시 |

#### 레이어별 기대결과 표현

같은 기능이라도 레이어마다 기대결과가 다르다. 각 레이어의 **실제 책임**에 맞는 결과를 명시한다.

| 레이어 | 기대결과 표현 | 예시 |
|---|---|---|
| **Domain** | 엔티티 상태 변화 | `쿠폰_생성_시_쿠폰이_생성된다`, `삭제_시_deletedAt이_설정된다` |
| **Service** | 반환값 (DTO) | `쿠폰_생성_시_쿠폰정보를_반환한다`, `조회_시_CouponInfo를_반환한다` |
| **Controller** | HTTP 응답 | `쿠폰_생성_시_쿠폰정보를_반환한다`, `쿠폰명이_비어있으면_400을_반환한다` |

#### 성공 케이스 - 구체적 결과를 명시

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `상품을_생성하면_성공한다` | `상품_생성_시_저장된_상품의_ProductInfo를_반환한다` | 반환값이 무엇인지 명시 |
| `상품을_수정하면_성공한다` | `상품_수정_시_변경된_정보가_반영된_ProductInfo를_반환한다` | 변경이 반영됨을 명시 |
| `상품을_삭제하면_성공한다` | `상품_삭제_시_deletedAt이_설정된다` | 상태 변화를 명시 |
| `상품_목록_조회에_성공한다` | `상품_목록_조회_시_삭제되지_않은_상품을_Page로_반환한다` | 필터링 조건과 반환 타입 명시 |

#### 실패 케이스 - 구체적 예외를 명시

| 나쁜 예 | 좋은 예 | 이유 |
|---|---|---|
| `예외가_발생한다` | `CoreException_NOT_FOUND가_발생한다` | 예외 클래스와 ErrorType 명시 |
| `실패한다` | `400을_반환한다` | HTTP 상태 코드 명시 (컨트롤러) |

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

**핵심 원칙: 구현이 아닌 행위를 테스트한다.**

행위 검증이란 "내부에서 어떤 메서드가 호출되었는가?"가 아니라,
**"사용자가 이 기능을 실행했을 때 기대하는 결과가 나타나는가?"**를 검증하는 것이다.

**Service 단위 테스트에서 검증할 것**
- 조건에 따른 예외 발생 분기 (존재하지 않는 엔티티 조회, 중복 검사 등)
- 조건에 따라 다른 로직을 실행하는 분기
- 비즈니스 규칙에 의한 판단/거부

**Service 단위 테스트에서 검증하지 않을 것 → 통합 테스트로**
- mock 반환값을 DTO로 변환한 뒤 필드별 비교 (구현 추적)
- 단일 repository 메서드를 위임 호출하는 단순 흐름
- `verify(save())`로 저장 호출 여부만 확인 (내부 구현 의존)

**판단 기준:** 서비스에 분기/판단 로직이 없고 단순히 저장 후 변환하는 흐름이라면, 단위 테스트보다 **통합 테스트**가 적합하다.

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // 좋은 예: 서비스의 판단/분기를 검증
    @Test
    void 존재하지_않는_상품_조회_시_CoreException_NOT_FOUND가_발생한다() {
        // given
        given(productRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(1L))
                .isInstanceOf(CoreException.class);
    }

    // 나쁜 예: mock에 넣은 값이 그대로 나오는지 확인 → 통합 테스트로
    // void 상품_생성_시_ProductInfo를_반환한다() { ... }
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

- 인증/인가(AdminAuthInterceptor 등)는 횡단 관심사이므로, 개별 컨트롤러 테스트에서 반복하지 않는다. 인터셉터 자체의 테스트에서 검증한다.
- Request DTO의 검증 어노테이션(`@NotNull`, `@NotBlank` 등)은 **각 API 엔드포인트별로** 모든 검증 필드에 대해 엣지케이스 테스트를 작성한다. 생성(POST)과 수정(PUT)이 동일한 검증이라도 별개의 API이므로 각각 테스트한다.
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

## 테스트 작성 원칙

### 1. Given-When-Then으로 가독성 확보

테스트 코드는 "무엇을 테스트하는지" 한눈에 들어와야 한다.

- **Given**: 테스트에 필요한 상태나 데이터를 준비
- **When**: 검증하려는 동작을 수행
- **Then**: 예상한 결과가 나왔는지 검증 (Assert)

각 구간을 `// given`, `// when`, `// then` 주석으로 명확히 구분한다.

### 2. 구현이 아닌 행위를 테스트

행위 검증이란 "내부에서 어떤 메서드가 호출되었는가?"가 아니라, **"사용자가 이 기능을 실행했을 때 기대하는 결과가 나타나는가?"**를 검증하는 것이다. 내부 필드, private 메서드, `verify(save())`에 의존하는 테스트는 리팩토링 시 기능은 정상인데 테스트가 깨지는 **취약한 테스트**가 된다.

- 단위 테스트: 예외 분기, 조건부 로직 등 **서비스의 판단**을 검증
- 통합 테스트: 결과값(필드 매핑, CRUD 흐름) 검증

### 3. 경계값 분석 (Edge Cases)

가장 많은 버그는 "경계"에서 발생한다. 다음 케이스를 반드시 고려한다.

- 값이 `0`이거나 `null`인 경우
- 리스트가 비어있거나 요소가 매우 많은 경우
- 문자열 길이가 최소/최대치인 경우
- 날짜가 연말, 윤년, 만료일 당일인 경우

### 4. 한 테스트에서 한 가지만 검증

하나의 `@Test` 메서드에서 너무 많은 것을 검증하지 않는다. 검증이 많아지면 테스트 이름이 모호해지고, 실패 시 원인 파악이 어려워진다. **단일 책임 원칙**을 테스트 코드에도 적용한다.

---

## 공통 주의사항

1. 소스 코드를 읽지 않은 상태에서 테스트를 작성하지 않는다.
2. 기존 테스트 파일이 있으면 반드시 읽어 컨벤션을 따른다.
3. 테스트 메서드명에 "성공한다", "실패한다" 같은 모호한 표현을 사용하지 않는다.
4. 테스트 코드 작성 전 반드시 Step 2의 설계 결과를 사용자에게 확인받는다.
5. 불필요한 테스트(getter 검증, 단순 위임, 반환값 필드 매핑 등)는 단위 테스트에서 작성하지 않는다.
6. 테스트 메서드명과 섹션 주석에 구현 메서드명을 사용하지 않는다. `// --- validateUsable 테스트 ---` 대신 `// --- 쿠폰 사용 가능 여부 검증 테스트 ---`처럼 비즈니스 행위로 표현한다.