package com.loopers.application.order;

import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.CreateBrandCommand;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.CreateCouponCommand;
import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.application.product.CreateProductCommand;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.user.CreateUserCommand;
import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserService;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;

import java.time.LocalDate;
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
        UserInfo userInfo = userService.createUser(
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        ProductInfo productInfo1 = productService.createProduct(brandInfo.id(), new CreateProductCommand(brandInfo.id(), "운동화", 50000, 10));
        ProductInfo productInfo2 = productService.createProduct(brandInfo.id(), new CreateProductCommand(brandInfo.id(), "슬리퍼", 30000, 5));

        // 주문 생성
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo1.id(), 2),
                new CreateOrderCommand.CreateOrderItemCommand(productInfo2.id(), 1)
        ));
        OrderInfo result = orderFacade.createOrder(command);

        // 주문 정보 검증
        assertThat(result.id()).isNotNull();
        assertThat(result.userId()).isEqualTo(userInfo.id());
        assertThat(result.totalAmount()).isEqualTo(130000);
        assertThat(result.orderItems()).hasSize(2);

        // 재고 차감 검증
        Product updatedProduct1 = productService.findProduct(productInfo1.id());
        Product updatedProduct2 = productService.findProduct(productInfo2.id());
        assertThat(updatedProduct1.getStock()).isEqualTo(8);
        assertThat(updatedProduct2.getStock()).isEqualTo(4);
    }

    @Test
    void 재고가_부족하면_주문_전체가_실패하고_재고가_변경되지_않는다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록 (재고 2개)
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        ProductInfo productInfo = productService.createProduct(brandInfo.id(), new CreateProductCommand(brandInfo.id(), "운동화", 50000, 2));

        // 재고 초과 주문
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo.id(), 5)
        ));

        // 주문 실패 검증
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문_취소_시_상태가_CANCELLED로_변경되고_재고가_복원된다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        ProductInfo productInfo = productService.createProduct(brandInfo.id(), new CreateProductCommand(brandInfo.id(), "운동화", 50000, 10));

        // 주문 생성
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo.id(), 2)
        ));
        OrderInfo orderResult = orderFacade.createOrder(command);

        // 재고 차감 확인
        Product deductedProduct = productService.findProduct(productInfo.id());
        assertThat(deductedProduct.getStock()).isEqualTo(8);

        // 주문 취소
        OrderInfo cancelResult = orderFacade.cancelOrder(userInfo.id(), orderResult.id());

        // 취소 상태 확인
        assertThat(cancelResult.status()).isEqualTo("CANCELLED");

        // 재고 복원 확인
        Product restoredProduct = productService.findProduct(productInfo.id());
        assertThat(restoredProduct.getStock()).isEqualTo(10);
    }

    @Test
    void 쿠폰을_적용하여_주문하면_할인이_반영되고_쿠폰이_사용_처리된다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        int productPrice = 50000;
        int orderQuantity = 2;
        int initialStock = 10;
        ProductInfo productInfo = productService.createProduct(brandInfo.id(),
                new CreateProductCommand(brandInfo.id(), "운동화", productPrice, initialStock));

        // 쿠폰 생성 + 발급
        int discountValue = 5000;
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand("정액 할인 쿠폰", DiscountType.FIXED, discountValue, null, LocalDate.now().plusDays(7)));
        UserCouponInfo userCouponInfo = couponService.issueCoupon(userInfo.id(), couponInfo.id());

        // 쿠폰 적용 주문 생성
        int expectedTotalAmount = productPrice * orderQuantity;
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), userCouponInfo.id(), List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo.id(), orderQuantity)
        ));
        OrderInfo result = orderFacade.createOrder(command);

        // 할인 금액 및 최종 결제 금액 검증
        assertThat(result.totalAmount()).isEqualTo(expectedTotalAmount);
        assertThat(result.discountAmount()).isEqualTo(discountValue);
        assertThat(result.finalAmount()).isEqualTo(expectedTotalAmount - discountValue);

        // 쿠폰 상태 USED 검증
        UserCouponInfo usedCoupon = couponService.getUserCoupons(userInfo.id()).stream()
                .filter(c -> c.id().equals(userCouponInfo.id()))
                .findFirst().orElseThrow();
        assertThat(usedCoupon.status()).isEqualTo("USED");

        // 재고 차감 검증
        Product updatedProduct = productService.findProduct(productInfo.id());
        assertThat(updatedProduct.getStock()).isEqualTo(initialStock - orderQuantity);
    }

    @Test
    void 쿠폰이_적용된_주문을_취소하면_쿠폰이_복원되고_재고가_복원된다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new CreateBrandCommand("나이키"));
        int initialStock = 10;
        int orderQuantity = 2;
        ProductInfo productInfo = productService.createProduct(brandInfo.id(),
                new CreateProductCommand(brandInfo.id(), "운동화", 50000, initialStock));

        // 쿠폰 생성 + 발급
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand("정액 할인 쿠폰", DiscountType.FIXED, 5000, null, LocalDate.now().plusDays(7)));
        UserCouponInfo userCouponInfo = couponService.issueCoupon(userInfo.id(), couponInfo.id());

        // 쿠폰 적용 주문 생성
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), userCouponInfo.id(), List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo.id(), orderQuantity)
        ));
        OrderInfo orderResult = orderFacade.createOrder(command);

        // 쿠폰 사용 상태 확인
        UserCouponInfo couponBeforeCancel = couponService.getUserCoupons(userInfo.id()).stream()
                .filter(c -> c.id().equals(userCouponInfo.id()))
                .findFirst().orElseThrow();
        assertThat(couponBeforeCancel.status()).isEqualTo("USED");

        // 주문 취소
        OrderInfo cancelResult = orderFacade.cancelOrder(userInfo.id(), orderResult.id());

        // 취소 상태 확인
        assertThat(cancelResult.status()).isEqualTo("CANCELLED");

        // 쿠폰 복원 확인
        UserCouponInfo restoredCoupon = couponService.getUserCoupons(userInfo.id()).stream()
                .filter(c -> c.id().equals(userCouponInfo.id()))
                .findFirst().orElseThrow();
        assertThat(restoredCoupon.status()).isEqualTo("AVAILABLE");

        // 재고 복원 확인
        Product restoredProduct = productService.findProduct(productInfo.id());
        assertThat(restoredProduct.getStock()).isEqualTo(initialStock);
    }
}
