package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventHandledCleanupScheduler {

    private static final int RETENTION_DAYS = 7;

    private final EventHandledRepository eventHandledRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanup() {
        ZonedDateTime before = ZonedDateTime.now().minusDays(RETENTION_DAYS);
        eventHandledRepository.deleteAllByCreatedAtBefore(before);
        log.info("EventHandled 정리 완료. {}일 이전 멱등성 기록 삭제", RETENTION_DAYS);
    }
}
