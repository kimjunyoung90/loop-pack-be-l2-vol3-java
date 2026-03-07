package com.loopers.concurrency;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 제어 검증: 비관적 락(PESSIMISTIC_WRITE)으로 쿠폰 중복 사용 방지
 *
 * 시나리오: 한 사용자가 여러 기기에서 동시에 같은 쿠폰을 사용
 * 기대 결과: 1건만 성공, 나머지는 이미 사용된 쿠폰으로 실패
 * 핵심: SELECT ... FOR UPDATE로 행 락을 잡아 동시 사용을 순차 처리
 */
@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class CouponConcurrencyIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Test
    void 동시에_같은_쿠폰을_사용하면_하나만_성공하고_나머지는_실패한다() throws InterruptedException {
        // given
        Long userId = 99999L;
        CouponResult coupon = couponService.registerCoupon(
                new CouponCreateCommand("동시성 테스트 쿠폰", DiscountType.FIXED, 3000, 10000, LocalDate.now().plusDays(7))
        );
        UserCouponResult issued = couponService.issueCoupon(userId, coupon.id());

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 10개 스레드가 동시에 같은 쿠폰을 사용
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponService.useCoupon(issued.id(), userId, 50000);
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

        // then - 1건만 성공하고 나머지는 실패해야 한다
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }
}
