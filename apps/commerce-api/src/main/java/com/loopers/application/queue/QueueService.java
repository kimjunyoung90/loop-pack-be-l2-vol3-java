package com.loopers.application.queue;

import com.loopers.application.queue.result.QueuePositionResult;
import com.loopers.application.queue.result.QueueTokenResult;
import com.loopers.domain.queue.QueuePosition;
import com.loopers.domain.queue.QueueRepository;
import com.loopers.domain.queue.QueueStatus;
import com.loopers.domain.queue.QueueToken;
import com.loopers.infrastructure.queue.QueueProperties;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class QueueService {

	private final QueueRepository queueRepository;
	private final QueueProperties queueProperties;

	private volatile boolean redisAvailable = true;

	public QueueTokenResult enterQueue(Long userId) {
		try {
			queueRepository.enqueue(new QueueToken(userId, System.currentTimeMillis()));

			Long rank = queueRepository.getRank(userId);
			long position = rank != null ? rank + 1 : 0;
			redisAvailable = true;
			return new QueueTokenResult("WAITING", position, calculateEstimatedWait(position), calculatePollingInterval(position));
		} catch (Exception e) {
			log.warn("Redis 장애로 대기열 진입을 건너뜁니다. userId={}", userId, e);
			redisAvailable = false;
			return QueueTokenResult.bypassed();
		}
	}

	public QueuePositionResult getQueuePosition(Long userId) {
		try {
			Long rank = queueRepository.getRank(userId);
			if (rank != null) {
				long position = rank + 1;
				long estimatedWaitSeconds = calculateEstimatedWait(position);
				long pollingInterval = calculatePollingInterval(position);
				QueuePosition queuePosition = new QueuePosition(QueueStatus.WAITING, position, estimatedWaitSeconds, pollingInterval, 0);
				redisAvailable = true;
				return QueuePositionResult.from(queuePosition, null);
			}

			String token = queueRepository.findTokenByUserId(userId).orElse(null);
			if (token != null) {
				long orderableAfterSeconds = calculateOrderableAfterSeconds(userId);
				QueuePosition queuePosition = new QueuePosition(QueueStatus.ALLOWED, 0, 0, 0, orderableAfterSeconds);
				redisAvailable = true;
				return QueuePositionResult.from(queuePosition, token);
			}

			redisAvailable = true;
			QueuePosition queuePosition = new QueuePosition(QueueStatus.NOT_FOUND, 0, 0, 0, 0);
			return QueuePositionResult.from(queuePosition, null);
		} catch (Exception e) {
			log.warn("Redis 장애로 대기열 순번 조회를 건너뜁니다. userId={}", userId, e);
			redisAvailable = false;
			QueuePosition queuePosition = new QueuePosition(QueueStatus.BYPASSED, 0, 0, 0, 0);
			return QueuePositionResult.from(queuePosition, null);
		}
	}

	public void validateToken(String token, Long userId) {
		try {
			String storedToken = queueRepository.findTokenByUserId(userId)
					.orElseThrow(() -> new CoreException(ErrorType.QUEUE_TOKEN_NOT_FOUND));

			if (!storedToken.equals(token)) {
				throw new CoreException(ErrorType.QUEUE_TOKEN_NOT_FOUND);
			}

			queueRepository.findOrderableAtByUserId(userId).ifPresent(orderableAt -> {
				if (System.currentTimeMillis() < orderableAt) {
					throw new CoreException(ErrorType.BAD_REQUEST, "아직 주문 가능 시간이 아닙니다.");
				}
			});
			redisAvailable = true;
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Redis 장애로 토큰 검증을 건너뜁니다. userId={}", userId, e);
			redisAvailable = false;
		}
	}

	public void removeToken(Long userId) {
		try {
			queueRepository.removeToken(userId);
			redisAvailable = true;
		} catch (Exception e) {
			log.warn("Redis 장애로 토큰 삭제를 건너뜁니다. userId={}", userId, e);
			redisAvailable = false;
		}
	}

	public boolean isRedisAvailable() {
		return redisAvailable;
	}

	private long calculateOrderableAfterSeconds(Long userId) {
		return queueRepository.findOrderableAtByUserId(userId)
				.map(orderableAt -> Math.max(0, (orderableAt - System.currentTimeMillis()) / 1000))
				.orElse(0L);
	}

	private long calculatePollingInterval(long position) {
		for (QueueProperties.Polling.Tier tier : queueProperties.polling().tiers()) {
			if (position <= tier.maxPosition()) {
				return tier.intervalSeconds();
			}
		}
		return queueProperties.polling().defaultIntervalSeconds();
	}

	private long calculateEstimatedWait(long position) {
		int batchSize = queueProperties.scheduler().batchSize();
		long intervalMs = queueProperties.scheduler().intervalMs();
		double secondsPerBatch = intervalMs / 1000.0;
		return (long) Math.ceil((double) position / batchSize * secondsPerBatch);
	}
}
