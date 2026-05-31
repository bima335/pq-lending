package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.valueobject.Money;
import java.util.List;

public class ValidatedState extends State {
    public ValidatedState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.VALIDATED;
    }

    @Override
    public void validate() {
        if (loan.getAmount() == null || loan.getAmount().getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount harus lebih dari 0");
        }
        if (loan.getGrade() == null) {
            throw new IllegalStateException("Grade borrower belum ditentukan");
        }
        if (loan.getAmount().getAmount().compareTo(loan.getGrade().getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }
        if (loan.getTenor() == null || !loan.getGrade().getAllowedTenors().contains(loan.getTenor())) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }
    }

    @Override
    public void startFunding() {
        loan.setFundingDeadline(java.time.LocalDate.now().plusDays(20));
        loan.setCurrentState(new FundingState(loan));
    }

    @Override
    public void cancel(Borrower borrower, List<Lender> lenders) {
        performCancel(borrower, lenders);
    }
}
