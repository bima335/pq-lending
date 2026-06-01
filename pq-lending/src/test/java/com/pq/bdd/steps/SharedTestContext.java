package com.pq.bdd.steps;

import com.pq.domain.model.borrower.Borrower;

public class SharedTestContext {
    private Borrower borrower;

    public Borrower getBorrower() {
        return borrower;
    }

    public void setBorrower(Borrower borrower) {
        this.borrower = borrower;
    }
}
