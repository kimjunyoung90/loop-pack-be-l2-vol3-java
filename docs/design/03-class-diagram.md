# 클래스 다이어그램

각 도메인의 책임 범위와 의존 방향을 확인하기 위한 다이어그램이다. Order가 Product를 직접 참조하지 않고 스냅샷(OrderItem)으로 분리한 것이 핵심 설계 결정이다.

```mermaid
classDiagram
    class User {
        -Long id
        -String loginId
        -String password
        -String name
        -String email
        -UserRole role
    }

    class UserRole {
        <<enumeration>>
        USER
        ADMIN
    }

    class Brand {
        -Long id
        -String name
        +delete()
    }

    class Product {
        -Long id
        -Long brandId
        -String name
        -int price
        -int stock
        +decreaseStock(quantity)
        +restoreStock(quantity)
        +delete()
    }

    class ProductLike {
        -Long id
        -Long userId
        -Long productId
    }

    class Order {
        -Long id
        -Long userId
        -OrderStatus status
        +cancel()
    }

    class OrderItem {
        -Long id
        -Long orderId
        -Long productId
        -String productName
        -int unitPrice
        -int quantity
    }

    class OrderStatus {
        <<enumeration>>
        COMPLETED
        CANCELED
    }

    User ..> UserRole
    User "1" -- "*" ProductLike : likes
    User "1" -- "*" Order : places
    Brand "1" *-- "*" Product : has
    Product "1" -- "*" ProductLike : receives
    Order "1" *-- "*" OrderItem : contains
    Order ..> OrderStatus
    Product "1" ..> "*" OrderItem : snapshot
```
