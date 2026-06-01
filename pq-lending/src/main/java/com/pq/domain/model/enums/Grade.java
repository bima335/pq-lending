package com.pq.domain.model.enums;

import com.pq.domain.model.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;

public enum Grade {
    A {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("500000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(
                Tenor.SIX, Tenor.TWELVE, Tenor.EIGHTEEN, Tenor.TWENTY_FOUR, Tenor.THIRTY_SIX
            );
        }
        public String getStrategyType() {
            return "EFFECTIVE";
        }
        public double getAnnualRate() {
            return 0.12;
        }
    },
    B {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("200000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(
                Tenor.SIX, Tenor.TWELVE, Tenor.EIGHTEEN, Tenor.TWENTY_FOUR
            );
        }
        public String getStrategyType() {
            return "EFFECTIVE";
        }
        public double getAnnualRate() {
            return 0.15;
        }
    },
    C {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("50000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(
                Tenor.SIX, Tenor.TWELVE, Tenor.EIGHTEEN
            );
        }
        public String getStrategyType() {
            return "FLAT";
        }
        public double getAnnualRate() {
            return 0.18;
        }
    },
    D {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("10000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(Tenor.SIX, Tenor.TWELVE);
        }
        public String getStrategyType() {
            return "FLAT";
        }
        public double getAnnualRate() {
            return 0.24;
        }
    };

    public abstract Money getMaxAmount();
    public abstract List<Tenor> getAllowedTenors();
    public abstract String getStrategyType();
    public abstract double getAnnualRate();
}
