package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.strategy.InterestStrategy;
import com.pq.domain.model.loan.strategy.InterestStrategyFactory;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.*;
import com.pq.domain.model.loan.state.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private Money amount;
    private Tenor tenor;
    private Grade grade;
    private InterestStrategy interestStrategy;
    private String strategyType;
    private State currentState;
    private LocalDate fundingDeadline;
    private final List<Funding> fundings;
    private final List<Payment> payments;
    private java.math.BigDecimal totalFunding;

    public Loan(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.currentState = new SubmittedState(this);
        this.fundings = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.totalFunding = java.math.BigDecimal.ZERO;
    }

    public void determineInterestStrategy(Grade borrowerGrade) {
        if (this.interestStrategy != null) {
            throw new IllegalStateException(
                    "Strategy has already been determined and is immutable.");
        }
        this.grade = borrowerGrade;
        this.interestStrategy = InterestStrategyFactory
                .create(borrowerGrade.getStrategyType());
        this.strategyType = this.interestStrategy.getClass().getSimpleName();
    }

    public InterestStrategy getInterestStrategy() {
        return interestStrategy;
    }

    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        this.currentState.submit(borrower, amount, tenor);
    }

    public void validate() {
        this.currentState.validate();
    }

    public void startFunding() {
        this.currentState.startFunding();
    }

    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        this.currentState.addFunding(lenderId, amount, lender);
    }

    public void cancel(Borrower borrower, List<Lender> lenders) {
        this.currentState.cancel(borrower, lenders);
    }

    public void disburse() {
        this.currentState.disburse();
    }

    public void makeRepayment(PaymentId paymentId, List<Lender> lenders, Money amount) {
        this.currentState.makeRepayment(paymentId, lenders, amount);
    }

    public void close() {
        this.currentState.close();
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setTenor(Tenor tenor) {
        this.tenor = tenor;
    }

    public void setFundingDeadline(LocalDate fundingDeadline) {
        this.fundingDeadline = fundingDeadline;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public void setState(LoanState state) {
        switch (state) {
            case SUBMITTED:
                this.currentState = new SubmittedState(this);
                break;
            case VALIDATED:
                this.currentState = new ValidatedState(this);
                break;
            case FUNDING:
                this.currentState = new FundingState(this);
                break;
            case CANCELLED:
                this.currentState = new CancelledState(this);
                break;
            case DISBURSED:
                this.currentState = new DisbursedState(this);
                break;
            case REPAYMENT:
                this.currentState = new RepaymentState(this);
                break;
            case CLOSED:
                this.currentState = new ClosedState(this);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    // Getters
    public LoanId getLoanId() {
        return loanId;
    }

    public BorrowerId getBorrowerId() {
        return borrowerId;
    }

    public Money getAmount() {
        return amount;
    }

    public Tenor getTenor() {
        return tenor;
    }

    public Grade getGrade() {
        return grade;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public LoanState getState() {
        return this.currentState.getLoanStateEnum();
    }

    public LocalDate getFundingDeadline() {
        return fundingDeadline;
    }

    public List<Funding> getFundings() {
        return fundings;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public Money getTotalFunded() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (Funding funding : fundings) {
            total = total.add(funding.getAmount().getAmount());
        }
        return new Money(total);
    }

    public double getFundingPercentage() {
        if (amount == null || amount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        java.math.BigDecimal fundedAmount = getTotalFunded().getAmount();
        java.math.BigDecimal targetAmount = amount.getAmount();
        return fundedAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100.0;
    }
}