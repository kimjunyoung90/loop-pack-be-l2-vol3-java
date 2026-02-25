package com.loopers.application.order;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.CreateBrandCommand;
import com.loopers.application.product.CreateProductCommand;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.user.CreateUserCommand;
import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserService;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
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
                new CreateUserCommand("testuser", "password1!", "홍길동", "1990-01-01", "test@test.com"));

        // 브랜드 + 상품 등록
        Brand brand = brandService.findBrand(
                brandService.createBrand(new CreateBrandCommand("나이키")).id());
        ProductInfo productInfo1 = productService.createProduct(brand, new CreateProductCommand(brand.getId(), "운동화", 50000, 10));
        ProductInfo productInfo2 = productService.createProduct(brand, new CreateProductCommand(brand.getId(), "슬리퍼", 30000, 5));

        // 주문 생성
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo1.id(), 2),
                new CreateOrderCommand.CreateOrderItemCommand(productInfo2.id(), 1)
        ));
        OrderInfo result = orderFacade.createOrder(command);

        // 주문 정보 검증
        assertThat(result.id()).isNotNull();
        assertThat(result.userId()).isEqualTo(userInfo.id());
        assertThat(result.totalPrice()).isEqualTo(130000);
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
        Brand brand = brandService.findBrand(
                brandService.createBrand(new CreateBrandCommand("나이키")).id());
        ProductInfo productInfo = productService.createProduct(brand, new CreateProductCommand(brand.getId(), "운동화", 50000, 2));

        // 재고 초과 주문
        CreateOrderCommand command = new CreateOrderCommand(userInfo.id(), List.of(
                new CreateOrderCommand.CreateOrderItemCommand(productInfo.id(), 5)
        ));

        // 주문 실패 검증
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
