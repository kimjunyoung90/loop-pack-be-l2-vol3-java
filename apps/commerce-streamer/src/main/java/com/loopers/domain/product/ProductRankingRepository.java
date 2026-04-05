package com.loopers.domain.product;

import java.util.Map;

public interface ProductRankingRepository {

    void incrementScores(Map<Long, Double> productScores);
}
