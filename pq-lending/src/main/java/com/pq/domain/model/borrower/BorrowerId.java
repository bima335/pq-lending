package com.p2plending.domain.model.borrower;

import java.util.Objects;

public class BorrowerId {
    private final String value;

    public BorrowerId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BorrowerId tidak boleh kosong");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BorrowerId)) return false;
        return value.equals(((BorrowerId) o).value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}