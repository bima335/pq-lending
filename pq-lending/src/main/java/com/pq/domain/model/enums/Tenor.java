package com.pq.domain.model.enums;

public enum Tenor {
    ONE(1), THREE(3), SIX(6), TWELVE(12), TWENTY_FOUR(24);

    private final int months;

    Tenor(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }

    public static Tenor fromMonths(int months) {
        for (Tenor t : values()) {
            if (t.months == months)
                return t;
        }
        throw new IllegalArgumentException(
                "Tenor tidak valid: " + months);
    }
}
