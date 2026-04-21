package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.CouponOutboxEventRepository;
import com.loopers.domain.outbox.LikeOutboxEventRepository;
import com.loopers.domain.outbox.OrderOutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import com.loopers.domain.outbox.ProductOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxCleanupScheduler {

    private static final int RETENTION_DAYS = 7;

    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final LikeOutboxEventRepository likeOutboxEventRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final CouponOutboxEventRepository couponOutboxEventRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        ZonedDateTime before = ZonedDateTime.now().minusDays(RETENTION_DAYS);
        productOutboxEventRepository.deleteAllByStatusAndCreatedAtBefore(OutboxStatus.PUBLISHED, before);
        likeOutboxEventRepository.deleteAllByStatusAndCreatedAtBefore(OutboxStatus.PUBLISHED, before);
        orderOutboxEventRepository.deleteAllByStatusAndCreatedAtBefore(OutboxStatus.PUBLISHED, before);
        couponOutboxEventRepository.deleteAllByStatusAndCreatedAtBefore(OutboxStatus.PUBLISHED, before);
        log.info("Outbox 정리 완료. {}일 이전 PUBLISHED 이벤트 삭제", RETENTION_DAYS);
    }
}
