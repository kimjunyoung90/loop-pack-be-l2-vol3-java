package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.Map;

public interface ProductRankingRepository {

    void incrementScores(LocalDate date, Map<Long, Double> productScores);

    void saveScores(LocalDate date, Map<Long, Double> productScores);

    Map<Long, Double> getAllScores(LocalDate date);
}
