package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private Money amount;
    private Tenor tenor;
    private Grade grade;
    private String strategyType;
    private LoanState state;
    private LocalDate fundingDeadline;
    private final List<Funding> fundings;
    private final List<Payment> payments;

    public Loan(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.state = LoanState.SUBMITTED;
        this.fundings = new ArrayList<>();
        this.payments = new ArrayList<>();
    }

    // TODO: semua method diimplementasi
    // oleh masing-masing anggota
    public void submit(Borrower borrower,
                       Money amount,
                       Tenor tenor) {
        this.amount = amount;
        this.tenor = tenor;
        this.grade = borrower.getCreditGrade();
    }

    public void validate() {
        this.state = LoanState.VALIDATED;
    }

    public void startFunding() {
        this.state = LoanState.FUNDING;
    }

    public void addFunding(LenderId lenderId,
                           Money amount,
                           Lender lender) {
        FundingId fundingId = new FundingId("funding-" + System.nanoTime());
        double portion = amount.getAmount().doubleValue() / this.amount.getAmount().doubleValue();
        Funding funding = new Funding(fundingId, lenderId, amount, portion);
        this.fundings.add(funding);
        
        // Set state menjadi DISBURSED ketika funding mencapai 100%
        if (getFundingPercentage() >= 100.0) {
            this.state = LoanState.DISBURSED;
        }
    }

    public void cancel(Borrower borrower,
                       List<Lender> lenders) {
        // Check state - tidak bisa cancel setelah DISBURSED
        if (this.state == LoanState.DISBURSED || this.state == LoanState.REPAYMENT || this.state == LoanState.CLOSED) {
            throw new RuntimeException("Loan tidak dapat dibatalkan setelah dana cair");
        }
        
        // Hitung denda berdasarkan funding percentage
        double fundingPercent = getFundingPercentage();
        BigDecimal totalTerkumpul = getTotalFunded().getAmount();
        BigDecimal denda = BigDecimal.ZERO;
        
        if (fundingPercent == 0) {
            denda = BigDecimal.ZERO;
        } else if (fundingPercent <= 50) {
            denda = totalTerkumpul.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.HALF_UP); // 1% dari total terkumpul
        } else {
            denda = totalTerkumpul.multiply(new BigDecimal("0.02")).setScale(0, RoundingMode.HALF_UP); // 2% dari total terkumpul
        }
        
        // Check saldo borrower cukup untuk denda
        if (borrower.getVirtualAccountBalance().getAmount().compareTo(denda) < 0) {
            throw new RuntimeException("Saldo tidak cukup untuk membayar denda");
        }
        
        // Potong saldo borrower
        if (denda.compareTo(BigDecimal.ZERO) > 0) {
            borrower.deductBalance(new Money(denda));
        }
        
        // Refund semua lender sesuai porsi mereka
        for (Funding funding : this.fundings) {
            for (Lender lender : lenders) {
                if (lender.getLenderId().equals(funding.getLenderId())) {
                    lender.addBalance(funding.getAmount());
                }
            }
        }
        
        this.state = LoanState.CANCELLED;
    }

    public void disburse() {
        if (getFundingPercentage() >= 100.0) {
            this.state = LoanState.DISBURSED;
            
            // Create payments sesuai tenor
            int numPayments = this.tenor.getMonths();
            BigDecimal perPayment = this.amount.getAmount()
                .divide(BigDecimal.valueOf(numPayments), RoundingMode.HALF_UP);
            
            for (int i = 1; i <= numPayments; i++) {
                PaymentId paymentId = new PaymentId("payment-" + i + "-" + System.nanoTime());
                Payment payment = new Payment(paymentId, new Money(perPayment));
                this.payments.add(payment);
            }
            
            this.state = LoanState.REPAYMENT;
        }
    }

    public void makeRepayment(PaymentId paymentId,
                              List<Lender> lenders) {
        // TODO: Anggota 5
    }

    public void close() {
        // TODO: Anggota 5
    }

    // Getters
    public LoanId getLoanId() { return loanId; }
    public BorrowerId getBorrowerId() { return borrowerId; }
    public Money getAmount() { return amount; }
    public Tenor getTenor() { return tenor; }
    public Grade getGrade() { return grade; }
    public String getStrategyType() { return strategyType; }
    public LoanState getState() { return state; }
    public LocalDate getFundingDeadline() {
        return fundingDeadline;
    }
    public List<Funding> getFundings() { return fundings; }
    public List<Payment> getPayments() { return payments; }

    public Money getTotalFunded() {
        BigDecimal total = BigDecimal.ZERO;
        for (Funding funding : fundings) {
            total = total.add(funding.getAmount().getAmount());
        }
        return new Money(total);
    }

    public double getFundingPercentage() {
        if (this.amount == null || this.amount.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal total = getTotalFunded().getAmount();
        return (total.doubleValue() / this.amount.getAmount().doubleValue()) * 100.0;
    }
}