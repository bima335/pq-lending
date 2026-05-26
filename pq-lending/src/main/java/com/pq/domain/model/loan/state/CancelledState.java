package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;

public class CancelledState extends State {
    public CancelledState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.CANCELLED;
    }
}
