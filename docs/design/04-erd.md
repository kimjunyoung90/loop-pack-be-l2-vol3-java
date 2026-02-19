# ERD

실제 테이블 구조와 관계, 제약 조건을 정의한다. 소프트 삭제, 좋아요 유니크 제약, 주문 상품 스냅샷 구조를 검증하기 위해 작성한다.

```mermaid
erDiagram
    users {
        bigint id PK
        varchar login_id
        varchar password
        varchar name
        varchar role
        timestamp created_at
        timestamp updated_at
    }

    brands {
        bigint id PK
        varchar name
        varchar description
        timestamp created_at
        timestamp updated_at
        boolean is_deleted
    }

    products {
        bigint id PK
        bigint brand_id FK
        varchar name
        int price
        int stock_quantity
        timestamp created_at
        timestamp updated_at
        boolean is_deleted
    }

    product_likes {
        bigint id PK
        bigint user_id FK
        bigint product_id FK
        timestamp created_at
    }

    orders {
        bigint id PK
        bigint user_id FK
        varchar status
        timestamp ordered_at
        timestamp updated_at
    }

    order_items {
        bigint id PK
        bigint order_id FK
        bigint product_id
        varchar product_name
        int unit_price
        int quantity
    }

    users ||--o{ product_likes : ""
    users ||--o{ orders : ""
    brands ||--o{ products : ""
    products ||--o{ product_likes : ""
    orders ||--o{ order_items : ""
```
