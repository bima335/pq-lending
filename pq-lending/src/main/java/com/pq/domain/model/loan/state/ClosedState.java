package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;

public class ClosedState extends State {
    public ClosedState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.CLOSED;
    }

    @Override
    public void cancel(com.pq.domain.model.borrower.Borrower borrower, java.util.List<com.pq.domain.model.lender.Lender> lenders) {
        throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
    }
}
