package com.p2plending.domain.model.lender;

import java.util.Objects;

public class LenderId {
    private final String value;

    public LenderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LenderId tidak boleh kosong");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LenderId)) return false;
        return value.equals(((LenderId) o).value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}