package com.loopers.application.order;

import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.command.OrderItemCreateCommand;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.user.UserService;
import com.loopers.application.user.command.UserCreateCommand;
import com.loopers.application.user.result.UserResult;
import com.loopers.domain.product.Product;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class OrderConcurrencyIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 동시성 제어 검증: 원자적 업데이트(Atomic Update)로 재고 정합성 보장
     *
     * 시나리오: 재고 100개인 상품에 100명이 동시에 1개씩 주문
     * 기대 결과: 100건 모두 성공, 재고 100 → 0
     * 핵심: UPDATE ... SET stock = stock - :quantity WHERE stock >= :quantity 로
     *       DB 레벨에서 원자적으로 차감하여 재고 갱신 손실(lost update)이 발생하지 않음
     */
    @Test
    void 동시에_같은_상품을_주문하면_재고가_정확히_차감된다() throws InterruptedException {
        // given
        BrandResult brand = brandService.createBrand(new BrandCreateCommand("나이키"));
        int initialStock = 100;
        ProductResult product = productService.createProduct(brand.id(),
                new ProductCreateCommand(brand.id(), "운동화", 50000, initialStock));

        int threadCount = 100;
        int quantityPerOrder = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100개 스레드가 동시에 같은 상품을 주문
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    UserResult user = userService.createUser(
                            new UserCreateCommand("ct" + index, "password1!", "사용자", "1990-01-01", "ct" + index + "@t.com"));

                    OrderCreateCommand command = new OrderCreateCommand(user.id(), null, List.of(
                            new OrderItemCreateCommand(product.id(), quantityPerOrder)
                    ));
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then - 100건 모두 성공하고, 재고가 정확히 100개 차감되어야 한다
        Product updatedProduct = productService.findProduct(product.id());
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(updatedProduct.getStock()).isEqualTo(initialStock - (successCount.get() * quantityPerOrder));
    }

    /**
     * 동시성 제어 검증: 여러 상품 주문 시 트랜잭션 원자성 보장
     *
     * 시나리오: 상품A(재고 100) + 상품B(재고 5)를 10명이 동시에 주문
     * 기대 결과:
     *   - 상품B 재고가 병목 → 선착순 5건만 성공, 나머지 5건은 상품B 재고 부족으로 실패
     *   - 실패한 주문은 트랜잭션 롤백으로 상품A 재고 차감도 취소됨
     *   - 최종 재고: 상품A = 95 (5건만 차감), 상품B = 0
     */
    @Test
    void 여러_상품_주문_시_하나라도_재고가_부족하면_주문이_실패하고_모든_재고가_롤백된다() throws InterruptedException {
        // given
        BrandResult brand = brandService.createBrand(new BrandCreateCommand("아디다스"));
        int productAStock = 100;
        int productBStock = 5;
        ProductResult productA = productService.createProduct(brand.id(),
                new ProductCreateCommand(brand.id(), "운동화", 50000, productAStock));
        ProductResult productB = productService.createProduct(brand.id(),
                new ProductCreateCommand(brand.id(), "슬리퍼", 30000, productBStock));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 10개 스레드가 동시에 상품A + 상품B를 함께 주문
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    UserResult user = userService.createUser(
                            new UserCreateCommand("st" + index, "password1!", "사용자", "1990-01-01", "st" + index + "@t.com"));

                    OrderCreateCommand command = new OrderCreateCommand(user.id(), null, List.of(
                            new OrderItemCreateCommand(productA.id(), 1),
                            new OrderItemCreateCommand(productB.id(), 1)
                    ));
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then - 상품B 재고(5)가 병목이므로 5건만 성공, 5건은 실패
        assertThat(successCount.get()).isEqualTo(productBStock);
        assertThat(failCount.get()).isEqualTo(threadCount - productBStock);

        // 실패한 주문의 상품A 재고도 롤백되어야 한다 (상품A: 100 → 95, 상품B: 5 → 0)
        Product updatedProductA = productService.findProduct(productA.id());
        Product updatedProductB = productService.findProduct(productB.id());
        assertThat(updatedProductA.getStock()).isEqualTo(productAStock - successCount.get());
        assertThat(updatedProductB.getStock()).isEqualTo(0);
    }
}
