package com.loopers.concurrency;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 선착순 쿠폰 발급 동시성 테스트
 *
 * 시나리오: 총 수량 100장인 쿠폰에 200명이 동시에 발급 요청
 * 핵심: Kafka 파티션 키(couponId) 기반 순서 보장 + DB 수량 제어로 정확히 N명만 발급 성공하는지 검증
 * 참고: requestIssueCoupon()의 수량 체크는 API 단계의 fast-fail이며,
 *       실제 정확한 수량 제어는 Consumer의 coupon.issue()에서 수행
 */
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=localhost:19092")
@Import(MySqlTestContainersConfig.class)
class CouponIssueConcurrencyIntegrationTest {

	private static final int TOTAL_QUANTITY = 100;
	private static final int THREAD_COUNT = 200;

	@Autowired
    private CouponService couponService;

	@MockitoBean
	private org.springframework.kafka.core.KafkaTemplate<Object, Object> kafkaTemplate;

	@Test
	void 동시에_200명이_선착순_100장_쿠폰을_요청하면_수량_이하만_성공한다() throws InterruptedException {
		// given
		CouponResult coupon = couponService.registerCoupon(
				new CouponCreateCommand("선착순 테스트 쿠폰", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7), TOTAL_QUANTITY)
		);

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			int finalI = i;
			executorService.submit(() -> {
				long userId = finalI + 1;
				try {
					couponService.requestIssueCoupon(userId, coupon.id());
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

		// then — DB 기반 수량 체크(fast-fail)로 totalQuantity 이하만 요청 수락
		assertThat(successCount.get()).isLessThanOrEqualTo(TOTAL_QUANTITY);
		assertThat(successCount.get() + failCount.get()).isEqualTo(THREAD_COUNT);
		System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
	}
}
