package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {

    boolean existsByEventId(String eventId);

    void deleteAllByCreatedAtBefore(ZonedDateTime before);
}
