package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.order.OrderFacade;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.infrastructure.kafka.message.OrderRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderRequestConsumer {

    private static final String ORDER_REQUEST_TOPIC = "order-requests";

    private final OrderFacade orderFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = ORDER_REQUEST_TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER,
            groupId = "order-request-consumer"
    )
    public void consumeOrderRequest(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            ConsumerRecord<Object, Object> record = messages.get(i);
            try {
                OrderRequestMessage message = objectMapper.readValue(
                        record.value().toString(), OrderRequestMessage.class);
                orderFacade.placeOrder(message.toCommand());
                log.info("degraded 주문 처리 완료. userId={}, idempotencyKey={}",
                        message.userId(), message.idempotencyKey());
            } catch (Exception e) {
                log.error("degraded 주문 처리 실패. record={}", record, e);
                throw new BatchListenerFailedException("degraded 주문 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }
}
