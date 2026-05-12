package com.pq.domain.model.valueobject;

public class PaymentId {
    private final String value;

    public PaymentId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
