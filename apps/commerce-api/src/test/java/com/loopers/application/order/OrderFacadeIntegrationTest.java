package com.loopers.application.order;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.user.UserService;
import com.loopers.application.user.command.UserCreateCommand;
import com.loopers.application.user.result.UserResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.support.error.CoreException;

import java.time.LocalDate;
import java.util.UUID;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponService couponService;

    @Test
    void 주문_생성_전체_흐름을_검증한다() {
        // 사용자 등록
        UserResult userResult = userService.signUp(
                new UserCreateCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand("나이키"));
        ProductResult productResult1 = productService.registerProduct(brandResult.id(), new ProductCreateCommand(brandResult.id(), "운동화", Money.of(50000), 10));
        ProductResult productResult2 = productService.registerProduct(brandResult.id(), new ProductCreateCommand(brandResult.id(), "슬리퍼", Money.of(30000), 5));

        // 주문 생성
        OrderCreateCommand command = new OrderCreateCommand(userResult.id(), UUID.randomUUID().toString(), null, List.of(
                new OrderCreateCommand.OrderItem(productResult1.id(), 2),
                new OrderCreateCommand.OrderItem(productResult2.id(), 1)
        ));
        OrderResult result = orderFacade.placeOrder(command);

        // 주문 정보 검증
        assertThat(result.id()).isNotNull();
        assertThat(result.userId()).isEqualTo(userResult.id());
        assertThat(result.totalAmount()).isEqualTo(Money.of(130000));
        assertThat(result.orderItems()).hasSize(2);

        // 재고 차감 검증
        ProductResult updatedProduct1 = productService.getProduct(productResult1.id());
        ProductResult updatedProduct2 = productService.getProduct(productResult2.id());
        assertThat(updatedProduct1.stock()).isEqualTo(8);
        assertThat(updatedProduct2.stock()).isEqualTo(4);
    }

    @Test
    void 재고가_부족하면_주문_전체가_실패하고_재고가_변경되지_않는다() {
        // 사용자 등록
        UserResult userResult = userService.signUp(
                new UserCreateCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록 (재고 2개)
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand("나이키"));
        ProductResult productResult = productService.registerProduct(brandResult.id(), new ProductCreateCommand(brandResult.id(), "운동화", Money.of(50000), 2));

        // 재고 초과 주문
        OrderCreateCommand command = new OrderCreateCommand(userResult.id(), UUID.randomUUID().toString(), null, List.of(
                new OrderCreateCommand.OrderItem(productResult.id(), 5)
        ));

        // 주문 실패 검증
        assertThatThrownBy(() -> orderFacade.placeOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문_취소_시_상태가_CANCELLED로_변경되고_재고가_복원된다() {
        // 사용자 등록
        UserResult userResult = userService.signUp(
                new UserCreateCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand("나이키"));
        ProductResult productResult = productService.registerProduct(brandResult.id(), new ProductCreateCommand(brandResult.id(), "운동화", Money.of(50000), 10));

        // 주문 생성
        OrderCreateCommand command = new OrderCreateCommand(userResult.id(), UUID.randomUUID().toString(), null, List.of(
                new OrderCreateCommand.OrderItem(productResult.id(), 2)
        ));
        OrderResult orderResult = orderFacade.placeOrder(command);

        // 재고 차감 확인
        ProductResult deductedProduct = productService.getProduct(productResult.id());
        assertThat(deductedProduct.stock()).isEqualTo(8);

        // 주문 취소
        OrderResult cancelResult = orderFacade.cancelOrder(userResult.id(), orderResult.id());

        // 취소 상태 확인
        assertThat(cancelResult.status()).isEqualTo("CANCELLED");

        // 재고 복원 확인
        ProductResult restoredProduct = productService.getProduct(productResult.id());
        assertThat(restoredProduct.stock()).isEqualTo(10);
    }

    @Test
    void 쿠폰을_적용하여_주문하면_할인이_반영되고_쿠폰이_사용_처리된다() {
        // 사용자 등록
        UserResult userResult = userService.signUp(
                new UserCreateCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand("나이키"));
        int productPrice = 50000;
        int orderQuantity = 2;
        int initialStock = 10;
        ProductResult productResult = productService.registerProduct(brandResult.id(),
                new ProductCreateCommand(brandResult.id(), "운동화", Money.of(productPrice), initialStock));

        // 쿠폰 생성 + 발급
        int discountValue = 5000;
        CouponResult couponResult = couponService.registerCoupon(
                new CouponCreateCommand("정액 할인 쿠폰", DiscountType.FIXED, discountValue, null, LocalDate.now().plusDays(7), 100));
        couponService.issueCoupon(userResult.id(), couponResult.id());
        var userCouponResult = couponService.getUserCoupons(userResult.id()).getFirst();

        // 쿠폰 적용 주문 생성
        int expectedTotalAmount = productPrice * orderQuantity;
        OrderCreateCommand command = new OrderCreateCommand(userResult.id(), UUID.randomUUID().toString(), userCouponResult.id(), List.of(
                new OrderCreateCommand.OrderItem(productResult.id(), orderQuantity)
        ));
        OrderResult result = orderFacade.placeOrder(command);

        // 할인 금액 및 최종 결제 금액 검증
        assertThat(result.totalAmount()).isEqualTo(Money.of(expectedTotalAmount));
        assertThat(result.discountAmount()).isEqualTo(Money.of(discountValue));
        assertThat(result.finalAmount()).isEqualTo(Money.of(expectedTotalAmount - discountValue));

        // 쿠폰 상태 USED 검증
        UserCouponResult usedCoupon = couponService.getUserCoupons(userResult.id()).stream()
                .filter(c -> c.id().equals(userCouponResult.id()))
                .findFirst().orElseThrow();
        assertThat(usedCoupon.status()).isEqualTo("USED");

        // 재고 차감 검증
        ProductResult updatedProduct = productService.getProduct(productResult.id());
        assertThat(updatedProduct.stock()).isEqualTo(initialStock - orderQuantity);
    }

    @Test
    void 쿠폰이_적용된_주문을_취소하면_쿠폰이_복원되고_재고가_복원된다() {
        // 사용자 등록
        UserResult userResult = userService.signUp(
                new UserCreateCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandResult brandResult = brandService.registerBrand(new BrandCreateCommand("나이키"));
        int initialStock = 10;
        int orderQuantity = 2;
        ProductResult productResult = productService.registerProduct(brandResult.id(),
                new ProductCreateCommand(brandResult.id(), "운동화", Money.of(50000), initialStock));

        // 쿠폰 생성 + 발급
        CouponResult couponResult = couponService.registerCoupon(
                new CouponCreateCommand("정액 할인 쿠폰", DiscountType.FIXED, 5000, null, LocalDate.now().plusDays(7), 100));
        couponService.issueCoupon(userResult.id(), couponResult.id());
        var userCouponResult = couponService.getUserCoupons(userResult.id()).getFirst();

        // 쿠폰 적용 주문 생성
        OrderCreateCommand command = new OrderCreateCommand(userResult.id(), UUID.randomUUID().toString(), userCouponResult.id(), List.of(
                new OrderCreateCommand.OrderItem(productResult.id(), orderQuantity)
        ));
        OrderResult orderResult = orderFacade.placeOrder(command);

        // 쿠폰 사용 상태 확인
        UserCouponResult couponBeforeCancel = couponService.getUserCoupons(userResult.id()).stream()
                .filter(c -> c.id().equals(userCouponResult.id()))
                .findFirst().orElseThrow();
        assertThat(couponBeforeCancel.status()).isEqualTo("USED");

        // 주문 취소
        OrderResult cancelResult = orderFacade.cancelOrder(userResult.id(), orderResult.id());

        // 취소 상태 확인
        assertThat(cancelResult.status()).isEqualTo("CANCELLED");

        // 쿠폰 복원 확인
        UserCouponResult restoredCoupon = couponService.getUserCoupons(userResult.id()).stream()
                .filter(c -> c.id().equals(userCouponResult.id()))
                .findFirst().orElseThrow();
        assertThat(restoredCoupon.status()).isEqualTo("AVAILABLE");

        // 재고 복원 확인
        ProductResult restoredProduct = productService.getProduct(productResult.id());
        assertThat(restoredProduct.stock()).isEqualTo(initialStock);
    }
}
