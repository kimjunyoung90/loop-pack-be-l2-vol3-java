package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import com.loopers.domain.queue.QueueToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.loopers.config.redis.RedisConfig.REDIS_TEMPLATE_MASTER;

@Slf4j
@Component
@EnableConfigurationProperties(QueueProperties.class)
public class QueueCacheManager implements QueueRepository {

	private static final String WAITING_QUEUE_KEY = "queue:waiting";
	private static final String ENTRY_TOKEN_KEY_PREFIX = "entry-token:";

	private final RedisTemplate<String, String> redisTemplate;
	private final Duration tokenTtl;

	public QueueCacheManager(
			@Qualifier(REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> redisTemplate,
			QueueProperties queueProperties
	) {
		this.redisTemplate = redisTemplate;
		this.tokenTtl = Duration.ofMinutes(queueProperties.token().ttlMinutes());
	}

	@Override
	public void enqueue(QueueToken token) {
		redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, String.valueOf(token.userId()), token.createdAt());
	}

	@Override
	public Optional<String> findTokenByUserId(Long userId) {
		String token = redisTemplate.opsForValue().get(entryTokenKey(userId));
		return Optional.ofNullable(token);
	}

	@Override
	public Long getRank(Long userId) {
		return redisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, String.valueOf(userId));
	}

	@Override
	public void popAndAllow(int count) {
		Set<ZSetOperations.TypedTuple<String>> popped = redisTemplate.opsForZSet()
				.popMin(WAITING_QUEUE_KEY, count);
		if (popped != null && !popped.isEmpty()) {
			for (ZSetOperations.TypedTuple<String> tuple : popped) {
				String userId = tuple.getValue();
				String token = UUID.randomUUID().toString();
				redisTemplate.opsForValue().set(entryTokenKey(Long.parseLong(userId)), token, tokenTtl);
			}
			log.info("대기열에서 {}명을 허용 상태로 이동했습니다.", popped.size());
		}
	}

	@Override
	public void removeToken(Long userId) {
		redisTemplate.delete(entryTokenKey(userId));
	}

	@Override
	public boolean isAlreadyQueued(Long userId) {
		return redisTemplate.opsForZSet().score(WAITING_QUEUE_KEY, String.valueOf(userId)) != null;
	}

	@Override
	public long getWaitingCount() {
		Long size = redisTemplate.opsForZSet().zCard(WAITING_QUEUE_KEY);
		return size != null ? size : 0;
	}

	private String entryTokenKey(Long userId) {
		return ENTRY_TOKEN_KEY_PREFIX + userId;
	}
}
