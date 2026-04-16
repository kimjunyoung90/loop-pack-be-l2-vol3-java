package com.loopers.application.ranking;

import com.loopers.application.ranking.result.ProductRankResult;
import com.loopers.domain.ranking.ProductRankRepository;
import com.loopers.domain.ranking.RankingCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RankingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingCacheRepository rankingCacheRepository;
    private final ProductRankRepository productRankRepository;

    @Transactional(readOnly = true)
    public List<Long> getRankedProductIds(String date, long offset, int size) {
        String resolvedDate = resolveDate(date);
        return rankingCacheRepository.getTopRankedProductIds(resolvedDate, offset, size);
    }

    @Transactional(readOnly = true)
    public long getTotalCount(String date) {
        String resolvedDate = resolveDate(date);
        return rankingCacheRepository.getTotalCount(resolvedDate);
    }

    public Long getRank(Long productId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return rankingCacheRepository.getRank(today, productId);
    }

    @Transactional(readOnly = true)
    public Page<ProductRankResult> getWeeklyRanks(Pageable pageable) {
        return productRankRepository.findLatestWeeklyRanks(pageable)
                .map(ProductRankResult::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductRankResult> getMonthlyRanks(Pageable pageable) {
        return productRankRepository.findLatestMonthlyRanks(pageable)
                .map(ProductRankResult::from);
    }

    private String resolveDate(String date) {
        return (date != null) ? date : LocalDate.now().format(DATE_FORMATTER);
    }
}
