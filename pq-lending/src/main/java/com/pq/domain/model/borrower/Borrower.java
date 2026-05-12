package com.pq.domain.model.borrower;

import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.valueobject.Money;

public class Borrower {
    private final BorrowerId borrowerId;
    private final String name;
    private final Grade creditGrade;
    private Money virtualAccountBalance;

    public Borrower(BorrowerId borrowerId, String name,
            Grade creditGrade,
            Money virtualAccountBalance) {
        this.borrowerId = borrowerId;
        this.name = name;
        this.creditGrade = creditGrade;
        this.virtualAccountBalance = virtualAccountBalance;
    }

    public BorrowerId getBorrowerId() {
        return borrowerId;
    }

    public String getName() {
        return name;
    }

    public Grade getCreditGrade() {
        return creditGrade;
    }

    public Money getVirtualAccountBalance() {
        return virtualAccountBalance;
    }

    public void deductBalance(Money amount) {
        if (amount.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Jumlah potongan tidak boleh negatif");
        }
        if (this.virtualAccountBalance.getAmount().compareTo(amount.getAmount()) < 0) {
            throw new IllegalStateException("Saldo tidak cukup untuk membayar denda");
        }
        this.virtualAccountBalance = new Money(
                this.virtualAccountBalance.getAmount().subtract(amount.getAmount()));
    }
}