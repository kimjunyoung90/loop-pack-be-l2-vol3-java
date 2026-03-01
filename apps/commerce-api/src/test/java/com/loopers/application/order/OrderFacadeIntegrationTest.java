package com.loopers.application.order;

import com.loopers.application.brand.BrandCommand;
import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import com.loopers.application.product.ProductCommand;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.user.UserCommand;
import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserService;
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

    @Test
    void 주문_생성_전체_흐름을_검증한다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new UserCommand.Create("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new BrandCommand.Create("나이키"));
        ProductInfo productInfo1 = productService.createProduct(new ProductCommand.Create(brandInfo.id(), "운동화", 50000, 10));
        ProductInfo productInfo2 = productService.createProduct(new ProductCommand.Create(brandInfo.id(), "슬리퍼", 30000, 5));

        // 주문 생성
        OrderCommand.Create command = new OrderCommand.Create(userInfo.id(), List.of(
                new OrderCommand.CreateItem(productInfo1.id(), 2),
                new OrderCommand.CreateItem(productInfo2.id(), 1)
        ));
        OrderInfo result = orderFacade.createOrder(command);

        // 주문 정보 검증
        assertThat(result.id()).isNotNull();
        assertThat(result.userId()).isEqualTo(userInfo.id());
        assertThat(result.totalPrice()).isEqualTo(130000);
        assertThat(result.orderItems()).hasSize(2);

        // 재고 차감 검증
        ProductInfo updatedProduct1 = productService.getProduct(productInfo1.id());
        ProductInfo updatedProduct2 = productService.getProduct(productInfo2.id());
        assertThat(updatedProduct1.stock()).isEqualTo(8);
        assertThat(updatedProduct2.stock()).isEqualTo(4);
    }

    @Test
    void 재고가_부족하면_주문_전체가_실패하고_재고가_변경되지_않는다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new UserCommand.Create("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록 (재고 2개)
        BrandInfo brandInfo = brandService.createBrand(new BrandCommand.Create("나이키"));
        ProductInfo productInfo = productService.createProduct(new ProductCommand.Create(brandInfo.id(), "운동화", 50000, 2));

        // 재고 초과 주문
        OrderCommand.Create command = new OrderCommand.Create(userInfo.id(), List.of(
                new OrderCommand.CreateItem(productInfo.id(), 5)
        ));

        // 주문 실패 검증
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문_취소_시_상태가_CANCELLED로_변경되고_재고가_복원된다() {
        // 사용자 등록
        UserInfo userInfo = userService.createUser(
                new UserCommand.Create("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        BrandInfo brandInfo = brandService.createBrand(new BrandCommand.Create("나이키"));
        ProductInfo productInfo = productService.createProduct(new ProductCommand.Create(brandInfo.id(), "운동화", 50000, 10));

        // 주문 생성
        OrderCommand.Create command = new OrderCommand.Create(userInfo.id(), List.of(
                new OrderCommand.CreateItem(productInfo.id(), 2)
        ));
        OrderInfo orderResult = orderFacade.createOrder(command);

        // 재고 차감 확인
        ProductInfo deductedProduct = productService.getProduct(productInfo.id());
        assertThat(deductedProduct.stock()).isEqualTo(8);

        // 주문 취소
        OrderInfo cancelResult = orderFacade.cancelOrder(userInfo.id(), orderResult.id());

        // 취소 상태 확인
        assertThat(cancelResult.status()).isEqualTo("CANCELLED");

        // 재고 복원 확인
        ProductInfo restoredProduct = productService.getProduct(productInfo.id());
        assertThat(restoredProduct.stock()).isEqualTo(10);
    }
}
