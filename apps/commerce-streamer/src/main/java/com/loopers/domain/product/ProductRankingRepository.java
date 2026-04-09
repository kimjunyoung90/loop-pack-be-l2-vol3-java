package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.Map;

public interface ProductRankingRepository {

    void incrementScores(Map<Long, Double> productScores);

    void saveScores(LocalDate date, Map<Long, Double> productScores);
}
