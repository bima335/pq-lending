package com.pq.domain.model.loan.strategy;

public class InterestStrategyFactory {
    public static InterestStrategy create(String strategyType) {
        switch (strategyType) {
            case "EFFECTIVE":
                return new EffectiveRateStrategy();
            case "FLAT":
                return new FlatRateStrategy();
            default:
                throw new IllegalStateException(
                        "Unknown strategy type: " + strategyType);
        }
    }
}
