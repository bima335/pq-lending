package com.pq.domain.model.valueobject;

import java.math.BigDecimal;

public class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount) {
        this.amount = amount;
        this.currency = "IDR";
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}