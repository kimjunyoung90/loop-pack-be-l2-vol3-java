package com.loopers.concurrency;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 제어 검증: 원자적 UPDATE로 좋아요 수 정합성 보장
 *
 * 시나리오: 여러 사용자가 동시에 같은 상품에 좋아요를 요청
 * 기대 결과: 모든 요청이 성공하고 likeCount가 정확히 반영
 * 핵심: UPDATE product SET like_count = like_count + 1로 Lost Update 방지
 */
@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class LikeConcurrencyIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Test
    void 동시에_여러_사용자가_좋아요를_요청해도_상품의_좋아요_수가_요청_수만큼_증가한다() throws InterruptedException {
        // given
        BrandResult brand = brandService.registerBrand(new BrandCreateCommand("동시성 테스트 브랜드"));
        ProductResult product = productService.registerProduct(brand.id(), new ProductCreateCommand(brand.id(), "동시성 테스트 상품", 10000, 100));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명의 사용자가 동시에 좋아요
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    likeFacade.like(userId, product.id());
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

        // then - 모든 좋아요가 성공하고, likeCount가 정확히 100
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        ProductResult updatedProduct = productService.getProduct(product.id());
        assertThat(updatedProduct.likeCount()).isEqualTo(threadCount);
    }

    @Test
    void 동시에_여러_사용자가_좋아요를_취소해도_상품의_좋아요_수가_요청_수만큼_감소한다() throws InterruptedException {
        // given
        BrandResult brand = brandService.registerBrand(new BrandCreateCommand("동시성 테스트 브랜드"));
        ProductResult product = productService.registerProduct(brand.id(), new ProductCreateCommand(brand.id(), "동시성 테스트 상품", 10000, 100));

        int threadCount = 100;

        // 먼저 100명이 순차적으로 좋아요 등록
        for (int i = 0; i < threadCount; i++) {
            likeFacade.like((long) (i + 1), product.id());
        }

        ProductResult likedProduct = productService.getProduct(product.id());
        assertThat(likedProduct.likeCount()).isEqualTo(threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명의 사용자가 동시에 좋아요 취소
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    likeFacade.unlike(userId, product.id());
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

        // then - 모든 취소가 성공하고, likeCount가 0
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        ProductResult updatedProduct = productService.getProduct(product.id());
        assertThat(updatedProduct.likeCount()).isEqualTo(0);
    }
}
