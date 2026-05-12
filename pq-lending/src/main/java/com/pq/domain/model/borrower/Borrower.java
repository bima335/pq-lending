package com.p2plending.domain.model.borrower;

import com.p2plending.domain.enums.Grade;
import com.p2plending.domain.model.valueobject.Money;

public class Borrower {
    private final BorrowerId borrowerId;
    private final String name;
    private final Grade creditGrade;
    private Money virtualAccountBalance;

    public Borrower(BorrowerId borrowerId, String name,
                    Grade creditGrade, Money virtualAccountBalance) {
        this.borrowerId = borrowerId;
        this.name = name;
        this.creditGrade = creditGrade;
        this.virtualAccountBalance = virtualAccountBalance;
    }

    // Dipakai saat potong denda cancel
    public void deductBalance(Money amount) {
        if (virtualAccountBalance.getAmount()
                .compareTo(amount.getAmount()) < 0) {
            throw new IllegalStateException(
                "Saldo borrower tidak cukup untuk membayar denda"
            );
        }
        this.virtualAccountBalance = virtualAccountBalance.subtract(amount);
    }

    public BorrowerId getBorrowerId() { return borrowerId; }
    public String getName() { return name; }
    public Grade getCreditGrade() { return creditGrade; }
    public Money getVirtualAccountBalance() { return virtualAccountBalance; }
}