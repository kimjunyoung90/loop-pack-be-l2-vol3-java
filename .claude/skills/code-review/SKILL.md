---
name: code-review
description:
  코드 리뷰를 수행하는 스킬. 프로젝트의 아키텍처 원칙, 도메인 설계 전략, 코딩 컨벤션을 기준으로
  변경된 코드를 체계적으로 검토한다. "코드 리뷰", "리뷰해줘", "코드 검토", "review" 등의 요청이 있을 때 이 스킬을 사용한다.
---

# Code Review Skill

변경된 코드를 아래 5가지 관점에서 순서대로 검토하고, 각 관점별로 결과를 출력한다.

```
관점 1. 아키텍처 준수 여부
관점 2. 도메인 설계 원칙
관점 3. DTO 분리 및 변환 패턴
관점 4. 코딩 컨벤션
관점 5. 잠재적 이슈
```

---

## 리뷰 대상 식별

리뷰를 시작하기 전에, 리뷰 대상 코드를 식별한다.

- 사용자가 특정 파일을 지정한 경우 → 해당 파일을 읽고 리뷰
- 사용자가 "변경된 코드", "작성한 코드" 등으로 지정한 경우 → `git diff --cached` 또는 `git diff` 로 변경 사항을 확인
- 사용자가 PR을 지정한 경우 → PR의 변경 파일을 확인
- 대상이 불명확한 경우 → 사용자에게 확인

리뷰 대상 코드를 읽은 후, 관련된 기존 코드(같은 도메인의 다른 레이어, 호출하는/호출되는 코드)도 함께 읽어 전체 맥락을 파악한다.

---

## 관점 1. 아키텍처 준수 여부

레이어드 아키텍처와 DIP(의존성 역전 원칙)을 준수하는지 검토한다.

### 검토 기준

| 규칙 | 설명 |
|---|---|
| **레이어 의존 방향** | interfaces → application → domain ← infrastructure. domain은 다른 레이어에 의존하지 않는다. |
| **DIP 준수** | domain 레이어에 Repository 인터페이스를 정의하고, infrastructure 레이어에서 구현체를 제공한다. |
| **패키지 구조** | `interfaces/api/{도메인}`, `application/{도메인}`, `domain/{도메인}`, `infrastructure/{도메인}` 구조를 따른다. |
| **레이어 간 import** | domain 패키지의 클래스가 application, interfaces, infrastructure 패키지를 import하지 않는다. |
| **기술 종속성 격리** | JPA 어노테이션은 domain/infrastructure에만, Spring Web 어노테이션은 interfaces에만 위치한다. |

### 출력 형식

```
### 관점 1. 아키텍처 준수 여부

| 심각도 | 파일 | 위치 | 내용 |
|---|---|---|---|
| 🔴 위반 | ProductService.java | L15 | domain 레이어의 클래스를 infrastructure에서 직접 참조 |
| 🟡 주의 | ... | ... | ... |
| 🟢 양호 | - | - | 레이어 의존 방향 준수 |
```

---

## 관점 2. 도메인 설계 원칙

도메인 객체가 비즈니스 규칙을 올바르게 캡슐화하는지 검토한다.

### 검토 기준

| 규칙 | 설명 |
|---|---|
| **비즈니스 규칙 캡슐화** | 엔티티 자신의 필드만으로 판단 가능한 규칙은 도메인 객체 내부에 위치해야 한다. |
| **guard() 활용** | 엔티티의 불변 조건(invariant)은 `guard()` 메서드를 오버라이드하여 PrePersist/PreUpdate 시점에 검증한다. |
| **생성자 보호** | `@NoArgsConstructor(access = PROTECTED)`와 `@Builder private` 생성자 패턴을 사용한다. |
| **상태 변경 메서드** | setter 대신 의미 있는 이름의 메서드(예: `update()`, `activate()`)를 통해 상태를 변경한다. |
| **Soft Delete** | `BaseEntity.delete()`를 활용하고, 삭제 시 `deletedAt`을 설정한다. |
| **규칙 위치 판단** | 서비스에 있는 로직이 도메인 객체에 속해야 하는지, 또는 그 반대인지 확인한다. |

### 판단 보조 질문

- "이 로직은 엔티티 자신의 필드만으로 판단 가능한가?" → Yes면 도메인 객체로 이동 고려
- "이 규칙이 여러 서비스에서 반복되는가?" → Yes면 도메인 객체에 속할 가능성 높음
- "외부 의존(DB 조회, 다른 도메인)이 필요한가?" → Yes면 서비스 레이어에 유지

### 출력 형식

```
### 관점 2. 도메인 설계 원칙

| 심각도 | 파일 | 위치 | 내용 |
|---|---|---|---|
| 🔴 위반 | ProductService.java | L25 | 가격 음수 검증 로직이 서비스에 위치 → 도메인 guard()로 이동 권장 |
| 🟡 주의 | Product.java | - | guard() 미구현: 필드 불변 조건이 있다면 추가 검토 필요 |
```

---

## 관점 3. DTO 분리 및 변환 패턴

API DTO와 Application DTO가 올바르게 분리되고 변환되는지 검토한다.

### 검토 기준

| 규칙 | 설명 |
|---|---|
| **API DTO 위치** | Request/Response DTO는 `interfaces/api/{도메인}` 패키지의 Dto 클래스 내부 중첩 record로 정의한다. |
| **Application DTO** | Command(입력)와 Info(출력) record를 `application/{도메인}` 패키지에 정의한다. |
| **DTO 분리** | Controller가 Application 레이어에 API DTO를 직접 전달하지 않는다. Command로 변환하여 전달한다. |
| **변환 메서드** | Info → Response 변환은 Response의 `from(Info)` 정적 팩토리를 사용한다. |
| **도메인 객체 노출 금지** | 도메인 엔티티가 Controller/Response에 직접 노출되지 않는다. 반드시 Info를 거친다. |
| **Validation 위치** | 입력값 포맷 검증(`@NotBlank`, `@Size` 등)은 API Request DTO에, `@Valid`는 Controller 파라미터에 위치한다. |

### 출력 형식

```
### 관점 3. DTO 분리 및 변환 패턴

| 심각도 | 파일 | 위치 | 내용 |
|---|---|---|---|
| 🔴 위반 | ProductV1Controller.java | L20 | CreateProductRequest를 직접 서비스에 전달 → Command 변환 필요 |
| 🟢 양호 | - | - | Info → Response 변환 패턴 준수 |
```

---

## 관점 4. 코딩 컨벤션

프로젝트의 코딩 컨벤션을 따르는지 검토한다.

### 검토 기준

| 규칙 | 설명 |
|---|---|
| **네이밍** | `docs/design/glossary.md`의 유비쿼터스 언어(영문명)를 기준으로 클래스, 메서드, 변수명을 결정한다. 리뷰 시 반드시 Glossary를 읽고 용어 일치 여부를 검토한다. 클래스 네이밍 패턴: Controller `{Domain}V1Controller`, Service `{Domain}Service`, Repository 인터페이스 `{Domain}Repository`, Repository 구현체 `{Domain}RepositoryImpl`, JPA `{Domain}JpaRepository` |
| **ApiSpec** | Controller는 `{Domain}V1ApiSpec` 인터페이스를 구현한다 (Swagger 문서화). |
| **ApiResponse 래핑** | 모든 Controller 응답은 `ApiResponse<T>`로 래핑한다. |
| **에러 처리** | `CoreException` + `ErrorType` 조합으로 에러를 발생시킨다. |
| **Soft Delete 필터링** | Repository 구현체의 조회 메서드는 `deletedAtIsNull` 조건을 포함한다. |
| **트랜잭션** | 조회는 `@Transactional(readOnly = true)`, 변경은 `@Transactional`을 사용한다. |
| **Lombok** | `@RequiredArgsConstructor`로 의존성 주입, `@Getter`는 도메인 엔티티에만, `@Setter` 사용 금지. |

### 출력 형식

```
### 관점 4. 코딩 컨벤션

| 심각도 | 파일 | 위치 | 내용 |
|---|---|---|---|
| 🟡 주의 | ProductRepositoryImpl.java | L15 | findAll()에 deletedAtIsNull 조건 누락 |
| 🟢 양호 | - | - | 네이밍 컨벤션 준수 |
```

---

## 관점 5. 잠재적 이슈

보안, 성능, 데이터 정합성 관점에서 잠재적 문제를 검토한다.

### 검토 기준

| 항목 | 설명 |
|---|---|
| **N+1 문제** | 연관 엔티티 조회 시 N+1 쿼리가 발생할 수 있는 구조인지 확인한다. |
| **트랜잭션 범위** | 불필요하게 넓은 트랜잭션 범위, 트랜잭션 내 외부 API 호출 등을 확인한다. |
| **동시성** | 동시 요청 시 데이터 정합성 문제가 발생할 수 있는지 확인한다. |
| **Null 안전성** | NullPointerException이 발생할 수 있는 지점을 확인한다. |
| **예외 처리** | 적절한 ErrorType을 사용하는지, 예외 메시지가 충분한지 확인한다. |
| **보안** | 인증/인가 검증 누락, 입력값 검증 누락 등을 확인한다. |

### 출력 형식

```
### 관점 5. 잠재적 이슈

| 심각도 | 파일 | 위치 | 내용 |
|---|---|---|---|
| 🔴 위험 | ProductV1Controller.java | - | 인가 검증(admin 체크) 누락 |
| 🟡 주의 | ProductService.java | L30 | 조회 후 수정 패턴에서 동시성 이슈 가능성 |
```

---

## 리뷰 결과 요약

모든 관점의 검토가 끝나면, 아래 형식으로 요약한다.

```
## 리뷰 요약

| 관점 | 🔴 위반 | 🟡 주의 | 🟢 양호 |
|---|---|---|---|
| 1. 아키텍처 | 0 | 0 | 3 |
| 2. 도메인 설계 | 1 | 1 | 2 |
| 3. DTO 패턴 | 0 | 1 | 3 |
| 4. 코딩 컨벤션 | 0 | 0 | 5 |
| 5. 잠재적 이슈 | 1 | 2 | 1 |

### 우선 조치 사항
1. [🔴 가장 심각한 위반 사항과 해결 방안]
2. [🔴 두 번째 심각한 위반 사항과 해결 방안]
3. ...
```

---

## 공통 주의사항

1. 코드를 직접 읽지 않은 상태에서 리뷰하지 않는다. 반드시 대상 파일과 관련 파일을 모두 읽는다.
2. 추측으로 문제를 제기하지 않는다. 실제 코드에서 확인된 사항만 보고한다.
3. 위반 사항에는 구체적인 파일명과 라인 번호를 명시한다.
4. 위반 사항에는 "왜 문제인지"와 "어떻게 개선할 수 있는지"를 함께 제시한다.
5. 기존 코드(리뷰 대상이 아닌 코드)의 문제는 별도로 분리하여 "기존 코드 참고 사항"으로 기록한다.
6. 단순 스타일 이슈(줄바꿈, 공백 등)는 보고하지 않는다.
