package com.pq.domain.model.loan.state;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import java.math.BigDecimal;

public class SubmittedState extends State {
    public SubmittedState(Loan loan) {
        super(loan);
    }

    @Override
    public LoanState getLoanStateEnum() {
        return LoanState.SUBMITTED;
    }

    @Override
    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        Grade borrowerGrade = borrower.getCreditGrade();

        if (amount == null || amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount harus lebih dari 0");
        }
        if (amount.getAmount().compareTo(new BigDecimal("1000000")) < 0) {
            throw new IllegalArgumentException("Amount kurang dari batas minimal");
        }

        if (amount.getAmount().compareTo(borrowerGrade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }

        if (tenor == null || !borrowerGrade.getAllowedTenors().contains(tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }

        loan.setAmount(amount);
        loan.setTenor(tenor);
        loan.determineStrategy(borrowerGrade);
        loan.setCurrentState(new ValidatedState(loan));
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
}
