# 클래스 다이어그램

각 도메인의 책임 범위와 의존 방향을 확인하기 위한 다이어그램이다. Order가 Product를 직접 참조하지 않고 스냅샷(OrderItem)으로 분리한 것이 핵심 설계 결정이다. UserCoupon은 Coupon과 독립적인 생명주기를 가진다.

```mermaid
%%{init: {'flowchart': {'curve': 'stepBefore'}}}%%
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
        -Long userCouponId
        -OrderStatus status
        -int totalAmount
        -int discountAmount
        -int finalAmount
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

    class Coupon {
        -Long id
        -String name
        -DiscountType discountType
        -int discountValue
        -Integer minOrderAmount
        -LocalDate expiredAt
        +calculateDiscount(totalAmount) int
        +validateIssuable()
        +delete()
    }

    class UserCoupon {
        -Long id
        -Long userId
        -Long couponId
        -CouponStatus status
        +validateUsable(userId, totalAmount)
        +calculateDiscount(totalAmount) int
        +use()
        +restore()
    }

    class DiscountType {
        <<enumeration>>
        FIXED
        RATE
    }

    class CouponStatus {
        <<enumeration>>
        AVAILABLE
        USED
        EXPIRED
    }

    User ..> UserRole
    User "1" -- "*" ProductLike : likes
    User "1" -- "*" Order : places
    User "1" -- "*" UserCoupon : owns
    Brand "1" *-- "*" Product : has
    Product "1" -- "*" ProductLike : receives
    Order "1" *-- "*" OrderItem : contains
    Order ..> OrderStatus
    Product "1" ..> "*" OrderItem : snapshot
    Coupon ..> DiscountType
    Order "0..1" -- "0..1" UserCoupon : applies
    Coupon "1" -- "*" UserCoupon : issues
    UserCoupon ..> CouponStatus

    class Payment {
        -Long id
        -Long orderId
        -Long userId
        -CardType cardType
        -String cardNo
        -Long amount
        -String transactionKey
        -PaymentStatus status
        -String failureReason
        +assignTransactionKey(transactionKey)
        +approve()
        +reject(reason)
        +unknown()
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        UNKNOWN
        APPROVED
        REJECTED
    }

    class CardType {
        <<enumeration>>
        SAMSUNG
        KB
        HYUNDAI
    }

    Payment ..> PaymentStatus
    Payment ..> CardType
    Order "1" -- "0..1" Payment : pays
    User "1" -- "*" Payment : requests
```
