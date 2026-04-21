package com.loopers.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.outbox.OrderOutboxEventService;
import com.loopers.infrastructure.kafka.message.OrderRequestMessage;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderRequestProducer {

    private static final String TOPIC = "order-requests";

    private final OrderOutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void send(OrderCreateCommand command) {
        try {
            OrderRequestMessage message = OrderRequestMessage.from(command);
            String payload = objectMapper.writeValueAsString(message);

            outboxEventService.saveAndPublish(
                    TOPIC,
                    String.valueOf(command.userId()),
                    payload
            );
            log.info("주문 요청 Outbox 저장 완료. userId={}, idempotencyKey={}", command.userId(), command.idempotencyKey());
        } catch (Exception e) {
            log.error("주문 요청 Outbox 저장 실패. userId={}, idempotencyKey={}", command.userId(), command.idempotencyKey(), e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 접수에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
