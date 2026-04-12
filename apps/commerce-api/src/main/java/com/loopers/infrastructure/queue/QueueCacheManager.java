package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import com.loopers.domain.queue.QueueToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.loopers.config.redis.RedisConfig.REDIS_TEMPLATE_MASTER;

@Slf4j
@Component
@EnableConfigurationProperties(QueueProperties.class)
public class QueueCacheManager implements QueueRepository {

	private static final String WAITING_QUEUE_KEY = "queue:waiting";
	private static final String ENTRY_TOKEN_KEY_PREFIX = "entry-token:";
	private static final String ORDERABLE_AT_KEY_PREFIX = "orderable-at:";

	private final RedisTemplate<String, String> redisTemplate;
	private final Duration tokenTtl;
	private final DefaultRedisScript<List> popAndIssueScript;

	public QueueCacheManager(
			@Qualifier(REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> redisTemplate,
			QueueProperties queueProperties
	) {
		this.redisTemplate = redisTemplate;
		this.tokenTtl = Duration.ofMinutes(queueProperties.token().ttlMinutes());
		this.popAndIssueScript = new DefaultRedisScript<>();
		this.popAndIssueScript.setLocation(new ClassPathResource("scripts/pop-and-issue-tokens.lua"));
		this.popAndIssueScript.setResultType(List.class);
	}

	@Override
	public boolean enqueue(QueueToken token) {
		Boolean added = redisTemplate.opsForZSet().addIfAbsent(WAITING_QUEUE_KEY, String.valueOf(token.userId()), token.createdAt());
		return Boolean.TRUE.equals(added);
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
	@SuppressWarnings("unchecked")
	public List<Long> popAndIssueTokens(int count, List<String> tokens, List<Long> orderableAts) {
		List<String> keys = List.of(WAITING_QUEUE_KEY);
		List<String> args = new ArrayList<>();
		args.add(ENTRY_TOKEN_KEY_PREFIX);
		args.add(ORDERABLE_AT_KEY_PREFIX);
		args.add(String.valueOf(count));
		args.add(String.valueOf(tokenTtl.getSeconds()));
		args.addAll(tokens);
		orderableAts.forEach(ts -> args.add(String.valueOf(ts)));

		List<String> result = redisTemplate.execute(popAndIssueScript, keys, (Object[]) args.toArray(new String[0]));
		if (result == null || result.isEmpty()) {
			return List.of();
		}
		return result.stream()
				.map(Long::parseLong)
				.toList();
	}

	@Override
	public Optional<Long> findOrderableAtByUserId(Long userId) {
		String value = redisTemplate.opsForValue().get(ORDERABLE_AT_KEY_PREFIX + userId);
		return Optional.ofNullable(value).map(Long::parseLong);
	}

	@Override
	public void removeToken(Long userId) {
		redisTemplate.delete(entryTokenKey(userId));
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
