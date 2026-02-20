# 시퀀스 다이어그램

## 목차

- [브랜드](#브랜드)
  - [브랜드 목록 조회 (사용자)](#브랜드-목록-조회-사용자)
  - [브랜드 상세 조회 (사용자)](#브랜드-상세-조회-사용자)
  - [브랜드 등록 (관리자)](#브랜드-등록-관리자)
  - [브랜드 목록 조회 (관리자)](#브랜드-목록-조회-관리자)
  - [브랜드 상세 조회 (관리자)](#브랜드-상세-조회-관리자)
  - [브랜드 수정 (관리자)](#브랜드-수정-관리자)
  - [브랜드 삭제 (관리자)](#브랜드-삭제-관리자)
- [상품](#상품)
  - [상품 목록 조회 (사용자)](#상품-목록-조회-사용자)
  - [상품 상세 조회 (사용자)](#상품-상세-조회-사용자)
  - [상품 등록 (관리자)](#상품-등록-관리자)
  - [상품 목록 조회 (관리자)](#상품-목록-조회-관리자)
  - [상품 상세 조회 (관리자)](#상품-상세-조회-관리자)
  - [상품 수정 (관리자)](#상품-수정-관리자)
  - [상품 삭제 (관리자)](#상품-삭제-관리자)
- [좋아요](#좋아요)
  - [좋아요 등록](#좋아요-등록)
  - [좋아요 취소](#좋아요-취소)
  - [좋아요한 상품 목록 조회](#좋아요한-상품-목록-조회)
- [주문](#주문)
  - [주문 생성](#주문-생성)
  - [주문 취소](#주문-취소)
  - [주문 목록 조회 (사용자)](#주문-목록-조회-사용자)
  - [주문 상세 조회 (사용자)](#주문-상세-조회-사용자)
  - [주문 목록 조회 (관리자)](#주문-목록-조회-관리자)
  - [주문 상세 조회 (관리자)](#주문-상세-조회-관리자)

---

## 공통: 인증 정책

모든 API 요청에는 아래 인증 정책이 적용된다. 개별 시퀀스 다이어그램에서는 인증 흐름을 생략한다.

| 대상 | 인증 방식 | 실패 응답 |
|------|-----------|-----------|
| 사용자 | X-Loopers-LoginId / X-Loopers-LoginPw 헤더 검증 | 401 Unauthorized |
| 관리자 | X-Loopers-Ldap: loopers.admin 헤더 검증 | 403 Forbidden |

---

## 브랜드

### 브랜드 목록 조회 (사용자)

사용자는 브랜드 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant BC as BrandController
    participant BS as BrandService
    participant BR as BrandRepository

    User->>+BC: 브랜드 목록 조회 요청
    BC->>+BS: 브랜드 목록 조회
    BS->>+BR: 브랜드 목록 조회
    BR-->>-BS: 브랜드 목록
    BS-->>-BC: 브랜드 목록
    BC-->>-User: 브랜드 목록 응답
```


---

### 브랜드 상세 조회 (사용자)

사용자는 특정 브랜드의 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant BC as BrandController
    participant BS as BrandService
    participant BR as BrandRepository

    User->>+BC: 브랜드 상세 조회 요청 (brandId)
    BC->>+BS: 브랜드 상세 조회(brandId)
    BS->>+BR: 브랜드 조회
    BR-->>-BS: 브랜드 정보

    opt 브랜드 미존재
        BS-->>BC: 예외
        BC-->>User: 404 Not Found
    end

    BS-->>-BC: 브랜드 상세 정보
    BC-->>-User: 브랜드 상세 응답
```

---

### 브랜드 등록 (관리자)

관리자는 새로운 브랜드를 등록할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant BC as BrandController
    participant BS as BrandService
    participant B as Brand
    participant BR as BrandRepository

    Admin->>+BC: 브랜드 등록 요청
    BC->>+BS: 브랜드 등록(name, description)
    BS->>+B: 브랜드 생성
    B-->>-BS: 생성 완료
    BS->>+BR: 브랜드 저장
    BR-->>-BS: 저장 완료
    BS-->>-BC: 등록된 브랜드 정보
    BC-->>-Admin: 브랜드 등록 응답
```

---

### 브랜드 목록 조회 (관리자)

관리자는 등록된 브랜드 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant BC as BrandController
    participant BS as BrandService
    participant BR as BrandRepository

    Admin->>+BC: 브랜드 목록 조회 요청
    BC->>+BS: 브랜드 목록 조회
    BS->>+BR: 브랜드 목록 조회
    BR-->>-BS: 브랜드 목록
    BS-->>-BC: 브랜드 목록
    BC-->>-Admin: 브랜드 목록 응답
```

---

### 브랜드 상세 조회 (관리자)

관리자는 특정 브랜드의 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant BC as BrandController
    participant BS as BrandService
    participant BR as BrandRepository

    Admin->>+BC: 브랜드 상세 조회 요청 (brandId)
    BC->>+BS: 브랜드 상세 조회(brandId)
    BS->>+BR: 브랜드 조회
    BR-->>-BS: 브랜드 정보

    opt 브랜드 미존재
        BS-->>BC: 예외
        BC-->>Admin: 404 Not Found
    end

    BS-->>-BC: 브랜드 상세 정보
    BC-->>-Admin: 브랜드 상세 응답
```

---

### 브랜드 수정 (관리자)

관리자는 브랜드 정보를 수정할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant BC as BrandController
    participant BS as BrandService
    participant B as Brand
    participant BR as BrandRepository

    Admin->>+BC: 브랜드 수정 요청 (brandId)
    BC->>+BS: 브랜드 수정(brandId, name, description)
    BS->>+BR: 브랜드 조회
    BR-->>-BS: 브랜드 정보

    opt 브랜드 미존재
        BS-->>BC: 예외
        BC-->>Admin: 404 Not Found
    end

    BS->>+B: 브랜드 정보 수정
    B-->>-BS: 수정 완료
    BS->>+BR: 변경사항 반영
    BR-->>-BS: 반영 완료
    BS-->>-BC: 수정된 브랜드 정보
    BC-->>-Admin: 브랜드 수정 응답
```

---

### 브랜드 삭제 (관리자)

관리자는 브랜드를 삭제할 수 있다. 브랜드 삭제 시 해당 브랜드에 속한 상품의 좋아요와 상품도 함께 삭제된다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant BC as BrandController
    participant BS as BrandService
    participant LS as LikeService
    participant PS as ProductService
    participant B as Brand
    participant LR as LikeRepository
    participant PR as ProductRepository
    participant BR as BrandRepository

    Admin->>+BC: 브랜드 삭제 요청 (brandId)
    BC->>+BS: 브랜드 삭제(brandId)

    rect rgb(255, 245, 238)
        Note over BS, BR: 트랜잭션
        BS->>+BR: 브랜드 조회
        BR-->>-BS: 브랜드 정보

        opt 브랜드 미존재
            BS-->>BC: 예외
            BC-->>Admin: 404 Not Found
        end

        BS->>+LS: 브랜드 상품 좋아요 전체 삭제 요청(brandId)
        LS->>+LR: 해당 브랜드 상품의 좋아요 삭제
        LR-->>-LS: 삭제 완료
        LS-->>-BS: 삭제 완료

        BS->>+PS: 브랜드 상품 전체 삭제 요청(brandId)
        PS->>+PR: 해당 브랜드 상품 삭제
        PR-->>-PS: 삭제 완료
        PS-->>-BS: 삭제 완료

        BS->>+B: 브랜드 삭제
        B-->>-BS: 삭제 완료
        BS->>+BR: 변경사항 반영
        BR-->>-BS: 반영 완료
    end

    BS-->>-BC: 삭제 완료
    BC-->>-Admin: 브랜드 삭제 응답
```

**해석**:
- 브랜드 삭제는 좋아요(하드 삭제) → 상품(소프트 삭제) → 브랜드(소프트 삭제) 순서로 캐스케이드 삭제된다.
- 하나의 트랜잭션 안에서 처리되어 일관성을 보장한다.

---

## 상품

### 상품 목록 조회 (사용자)

사용자는 상품 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant PC as ProductController
    participant PS as ProductService
    participant PR as ProductRepository

    User->>+PC: 상품 목록 조회 요청
    PC->>+PS: 상품 목록 조회
    PS->>+PR: 상품 목록 조회
    PR-->>-PS: 상품 목록
    PS-->>-PC: 상품 목록
    PC-->>-User: 상품 목록 응답
```


---

### 상품 상세 조회 (사용자)

사용자는 특정 상품의 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant PC as ProductController
    participant PS as ProductService
    participant PR as ProductRepository

    User->>+PC: 상품 상세 조회 요청 (productId)
    PC->>+PS: 상품 상세 조회(productId)
    PS->>+PR: 상품 조회
    PR-->>-PS: 상품 정보

    opt 상품 미존재
        PS-->>PC: 예외
        PC-->>User: 404 Not Found
    end

    PS-->>-PC: 상품 상세 정보
    PC-->>-User: 상품 상세 응답
```

---

### 상품 등록 (관리자)

관리자는 새로운 상품을 등록할 수 있다. 상품이 속할 브랜드가 사전에 등록되어 있어야 한다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as ProductController
    participant PS as ProductService
    participant BS as BrandService
    participant P as Product
    participant BR as BrandRepository
    participant PR as ProductRepository

    Admin->>+PC: 상품 등록 요청
    PC->>+PS: 상품 등록(brandId, name, price, stockQuantity)
    PS->>+BS: 브랜드 존재 여부 확인(brandId)
    BS->>+BR: 브랜드 조회
    BR-->>-BS: 브랜드 정보
    BS-->>-PS: 브랜드 정보

    opt 브랜드 미존재
        PS-->>PC: 예외
        PC-->>Admin: 404 Not Found
    end

    PS->>+P: 상품 생성 (초기 재고 수량 포함)
    P-->>-PS: 생성 완료
    PS->>+PR: 상품 저장
    PR-->>-PS: 저장 완료
    PS-->>-PC: 등록된 상품 정보
    PC-->>-Admin: 상품 등록 응답
```

---

### 상품 목록 조회 (관리자)

관리자는 등록된 상품 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as ProductController
    participant PS as ProductService
    participant PR as ProductRepository

    Admin->>+PC: 상품 목록 조회 요청
    PC->>+PS: 상품 목록 조회
    PS->>+PR: 상품 목록 조회
    PR-->>-PS: 상품 목록
    PS-->>-PC: 상품 목록
    PC-->>-Admin: 상품 목록 응답
```

---

### 상품 상세 조회 (관리자)

관리자는 특정 상품의 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as ProductController
    participant PS as ProductService
    participant PR as ProductRepository

    Admin->>+PC: 상품 상세 조회 요청 (productId)
    PC->>+PS: 상품 상세 조회(productId)
    PS->>+PR: 상품 조회
    PR-->>-PS: 상품 정보

    opt 상품 미존재
        PS-->>PC: 예외
        PC-->>Admin: 404 Not Found
    end

    PS-->>-PC: 상품 상세 정보
    PC-->>-Admin: 상품 상세 응답
```

---

### 상품 수정 (관리자)

관리자는 상품 정보를 수정할 수 있다. 재고 수량도 수정 가능하다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as ProductController
    participant PS as ProductService
    participant P as Product
    participant PR as ProductRepository

    Admin->>+PC: 상품 수정 요청 (productId)
    PC->>+PS: 상품 수정(productId, name, price, stockQuantity)
    PS->>+PR: 상품 조회
    PR-->>-PS: 상품 정보

    opt 상품 미존재
        PS-->>PC: 예외
        PC-->>Admin: 404 Not Found
    end

    PS->>+P: 상품 정보 수정 (재고 수량 포함)
    P-->>-PS: 수정 완료
    PS->>+PR: 변경사항 반영
    PR-->>-PS: 반영 완료
    PS-->>-PC: 수정된 상품 정보
    PC-->>-Admin: 상품 수정 응답
```

---

### 상품 삭제 (관리자)

관리자는 상품을 삭제할 수 있다. 상품 삭제 시 해당 상품의 좋아요도 함께 삭제된다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as ProductController
    participant PS as ProductService
    participant LS as LikeService
    participant P as Product
    participant LR as LikeRepository
    participant PR as ProductRepository

    Admin->>+PC: 상품 삭제 요청 (productId)
    PC->>+PS: 상품 삭제(productId)

    rect rgb(255, 245, 238)
        Note over PS, PR: 트랜잭션
        PS->>+PR: 상품 조회
        PR-->>-PS: 상품 정보

        opt 상품 미존재
            PS-->>PC: 예외
            PC-->>Admin: 404 Not Found
        end

        PS->>+LS: 상품 좋아요 전체 삭제 요청(productId)
        LS->>+LR: 해당 상품의 좋아요 삭제
        LR-->>-LS: 삭제 완료
        LS-->>-PS: 삭제 완료

        PS->>+P: 상품 삭제
        P-->>-PS: 삭제 완료
        PS->>+PR: 변경사항 반영
        PR-->>-PS: 반영 완료
    end

    PS-->>-PC: 삭제 완료
    PC-->>-Admin: 상품 삭제 응답
```

**해석**:
- 상품 삭제는 좋아요(하드 삭제) → 상품(소프트 삭제) 순서로 캐스케이드 삭제된다.
- 하나의 트랜잭션 안에서 처리되어 일관성을 보장한다.

---

## 좋아요

### 좋아요 등록

사용자는 특정 상품에 좋아요를 할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant LC as LikeController
    participant LS as LikeService
    participant PS as ProductService
    participant PR as ProductRepository
    participant LR as LikeRepository

    User->>+LC: 좋아요 요청 (productId)
    LC->>+LS: 좋아요 등록(userId, productId)
    LS->>+PS: 상품 존재 여부 확인(productId)
    PS->>+PR: 상품 조회
    PR-->>-PS: 상품 정보
    PS-->>-LS: 상품 정보

    opt 상품 미존재
        LS-->>LC: 예외
        LC-->>User: 404 Not Found
    end

    LS->>+LR: 좋아요 중복 확인 (userId, productId)
    LR-->>-LS: 조회 결과

    opt 이미 좋아요한 상품
        LS-->>LC: 예외
        LC-->>User: 409 Conflict
    end

    LS->>+LR: 좋아요 저장
    LR-->>-LS: 저장 완료
    LS-->>-LC: 좋아요 완료
    LC-->>-User: 좋아요 응답
```

**해석**:
- 동일 상품에 대한 중복 좋아요는 허용되지 않는다.
- 좋아요 수는 별도 필드에 저장하지 않고, 조회 시 `product_likes` 테이블에서 COUNT 집계로 계산한다.

---

### 좋아요 취소

사용자는 자신이 좋아요한 상품의 좋아요를 취소할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant LC as LikeController
    participant LS as LikeService
    participant LR as LikeRepository

    User->>+LC: 좋아요 취소 요청 (productId)
    LC->>+LS: 좋아요 취소(userId, productId)
    LS->>+LR: 좋아요 조회 (userId, productId)
    LR-->>-LS: 조회 결과

    opt 좋아요하지 않은 상품
        LS-->>LC: 예외
        LC-->>User: 404 Not Found
    end

    LS->>+LR: 좋아요 삭제
    LR-->>-LS: 삭제 완료
    LS-->>-LC: 좋아요 취소 완료
    LC-->>-User: 좋아요 취소 응답
```

**해석**:
- 좋아요하지 않은 상품에 대한 취소 요청은 예외 처리된다.
- 좋아요 삭제는 하드 삭제(DELETE)로 처리된다.

---

### 좋아요한 상품 목록 조회

사용자는 자신이 좋아요한 상품 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant LC as LikeController
    participant LS as LikeService
    participant LR as LikeRepository

    User->>+LC: 좋아요한 상품 목록 조회 요청
    LC->>+LS: 좋아요한 상품 목록 조회(userId)
    LS->>+LR: 좋아요한 상품 목록 조회 (userId)
    LR-->>-LS: 상품 목록
    LS-->>-LC: 상품 목록
    LC-->>-User: 좋아요한 상품 목록 응답
```

---

## 주문

### 주문 생성

사용자는 여러 상품을 한 번에 주문할 수 있다. 단일 트랜잭션에서 재고 검증, 재고 차감, 주문 생성을 처리한다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant OC as OrderController
    participant OS as OrderService
    participant PS as ProductService
    participant P as Product
    participant O as Order
    participant PR as ProductRepository
    participant OR as OrderRepository

    User->>+OC: 주문 생성 요청 (상품 목록, 수량)
    OC->>+OS: 주문 생성(userId, orderItems)

    rect rgb(240, 248, 255)
        Note over OS, OR: 트랜잭션
        OS->>+PS: 전체 상품 조회
        PS->>+PR: 상품 조회
        PR-->>-PS: 상품 목록
        PS-->>-OS: 상품 목록

        opt 존재하지 않는 상품 포함
            OS-->>OC: 예외
        end

        loop 주문 상품마다
            OS->>+PS: 재고 차감 요청(상품, 주문수량)
            PS->>+P: 재고 차감(주문수량)
            P->>P: 재고 충분 여부 검증
            P->>P: 실제 재고 차감
            P-->>-PS: 차감 완료
            PS-->>-OS: 차감 완료
        end

        opt 하나라도 재고 부족
            Note over OS, OR: 롤백 → 전부 원복
            OS-->>OC: 예외 (전부 아니면 전무)
        end

        OS->>+O: 주문 생성 (상태: COMPLETED)
        O->>O: 주문 항목 생성 (상품명, 단가 스냅샷)
        O-->>-OS: 주문 생성 완료
        OS->>+OR: 변경사항 반영
        OR-->>-OS: 반영 완료
    end

    OS-->>-OC: 주문 완료
    OC-->>-User: 주문 완료 응답
```

**해석**:
- 단일 트랜잭션에서 재고 검증 → 재고 차감 → 주문 생성(COMPLETED)을 처리한다. 실패 시 전부 롤백된다.
- `Product`가 재고 충분 여부 검증과 재고 차감을 담당한다. 재고 관련 비즈니스 로직이 도메인 객체에 있다.
- `Order` 생성 시 `OrderItem`에 상품 스냅샷(상품명, 단가)을 보관한다. 이후 상품 정보가 변경되어도 주문 이력은 유지된다.

---

### 주문 취소

주문 취소 시 실제 재고를 복원한다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant OC as OrderController
    participant OS as OrderService
    participant PS as ProductService
    participant P as Product
    participant O as Order
    participant OR as OrderRepository

    User->>+OC: 주문 취소 요청 (orderId)
    OC->>+OS: 주문 취소(userId, orderId)

    rect rgb(255, 245, 238)
        Note over OS, OR: 트랜잭션
        OS->>+OR: 주문 조회
        OR-->>-OS: 주문 정보

        opt 주문 미존재 or 본인 아님
            OS-->>OC: 예외
        end

        OS->>+O: 주문 취소 요청
        O->>O: 취소 가능 상태 검증 (COMPLETED만 가능)

        opt 이미 취소된 주문
            OS-->>OC: 예외
        end

        O->>O: 주문 상태 변경 (COMPLETED → CANCELED)
        O-->>-OS: 상태 변경 완료

        loop 주문 상품마다
            OS->>+PS: 재고 복원 요청(상품, 주문수량)
            PS->>+P: 실제 재고 복원(주문수량)
            P->>P: 실제 재고 증가
            P-->>-PS: 복원 완료
            PS-->>-OS: 복원 완료
        end

        OS->>+OR: 변경사항 반영
        OR-->>-OS: 반영 완료
    end

    OS-->>-OC: 취소 완료
    OC-->>-User: 취소 완료 응답
```

---

### 주문 목록 조회 (사용자)

사용자는 본인의 주문 내역을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant OC as OrderController
    participant OS as OrderService
    participant OR as OrderRepository

    User->>+OC: 주문 목록 조회 요청
    OC->>+OS: 주문 목록 조회(userId)
    OS->>+OR: 본인 주문 목록 조회
    OR-->>-OS: 주문 목록
    OS-->>-OC: 주문 목록
    OC-->>-User: 주문 목록 응답
```

**해석**:
- 본인의 주문만 조회 가능하다.

---

### 주문 상세 조회 (사용자)

사용자는 본인의 특정 주문 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor User as 사용자
    participant OC as OrderController
    participant OS as OrderService
    participant OR as OrderRepository

    User->>+OC: 주문 상세 조회 요청 (orderId)
    OC->>+OS: 주문 상세 조회(userId, orderId)
    OS->>+OR: 주문 조회 (주문 항목 포함)
    OR-->>-OS: 주문 정보

    opt 주문 미존재
        OS-->>OC: 예외
        OC-->>User: 404 Not Found
    end

    opt 본인 주문 아님
        OS-->>OC: 예외
        OC-->>User: 403 Forbidden
    end

    OS-->>-OC: 주문 상세 정보 (주문 상품 스냅샷 포함)
    OC-->>-User: 주문 상세 응답
```

**해석**:
- 본인의 주문만 조회 가능하다. 타 사용자의 주문 접근 시 예외 처리된다.
- 주문 상세에는 OrderItem의 스냅샷(상품명, 단가)이 포함된다.

---

### 주문 목록 조회 (관리자)

관리자는 전체 사용자의 주문 목록을 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant OC as OrderController
    participant OS as OrderService
    participant OR as OrderRepository

    Admin->>+OC: 주문 목록 조회 요청
    OC->>+OS: 전체 주문 목록 조회
    OS->>+OR: 전체 주문 목록 조회
    OR-->>-OS: 주문 목록
    OS-->>-OC: 주문 목록
    OC-->>-Admin: 주문 목록 응답
```

---

### 주문 상세 조회 (관리자)

관리자는 특정 주문의 상세 정보를 조회할 수 있다.

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant OC as OrderController
    participant OS as OrderService
    participant OR as OrderRepository

    Admin->>+OC: 주문 상세 조회 요청 (orderId)
    OC->>+OS: 주문 상세 조회(orderId)
    OS->>+OR: 주문 조회 (주문 항목 포함)
    OR-->>-OS: 주문 정보

    opt 주문 미존재
        OS-->>OC: 예외
        OC-->>Admin: 404 Not Found
    end

    OS-->>-OC: 주문 상세 정보 (주문 상품 스냅샷 포함)
    OC-->>-Admin: 주문 상세 응답
```
