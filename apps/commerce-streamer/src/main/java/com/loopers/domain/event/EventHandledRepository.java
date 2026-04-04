package com.loopers.domain.event;

import java.time.ZonedDateTime;

public interface EventHandledRepository {

    boolean existsByEventId(String eventId);

    EventHandled save(EventHandled eventHandled);

    void deleteAllByCreatedAtBefore(ZonedDateTime before);
}
