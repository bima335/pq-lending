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
                Tenor.THREE, Tenor.SIX,
                Tenor.TWELVE, Tenor.TWENTY_FOUR
            );
        }
        public String getStrategyType() {
            return "EFFECTIVE";
        }
    },
    B {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("200000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(
                Tenor.THREE, Tenor.SIX, Tenor.TWELVE
            );
        }
        public String getStrategyType() {
            return "EFFECTIVE";
        }
    },
    C {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("50000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(
                Tenor.ONE, Tenor.THREE, Tenor.SIX
            );
        }
        public String getStrategyType() {
            return "FLAT";
        }
    },
    D {
        public Money getMaxAmount() {
            return new Money(new BigDecimal("10000000"));
        }
        public List<Tenor> getAllowedTenors() {
            return List.of(Tenor.ONE, Tenor.THREE);
        }
        public String getStrategyType() {
            return "FLAT";
        }
    };

    public abstract Money getMaxAmount();
    public abstract List<Tenor> getAllowedTenors();
    public abstract String getStrategyType();
}
