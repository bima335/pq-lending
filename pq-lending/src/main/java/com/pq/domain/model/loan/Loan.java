package com.pq.domain.model.loan;

import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.strategy.InterestStrategy;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.*;

import java.math.BigDecimal;
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
    private LoanState state;
    private LocalDate fundingDeadline;
    private final List<Funding> fundings;
    private final List<Payment> payments;
    private java.math.BigDecimal totalFunding;

    public Loan(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.state = LoanState.SUBMITTED;
        this.fundings = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.totalFunding = java.math.BigDecimal.ZERO;
    }

    public void determineInterestStrategy(Grade borrowerGrade) {
        if (this.interestStrategy != null) {
            throw new IllegalStateException("Strategy has already been determined and is immutable.");
        }
        this.grade = borrowerGrade;
        if (borrowerGrade == Grade.A || borrowerGrade == Grade.B) {
            this.interestStrategy = new EffectiveRateStrategy();
        } else if (borrowerGrade == Grade.C || borrowerGrade == Grade.D) {
            this.interestStrategy = new FlatRateStrategy();
        } else {
            throw new IllegalArgumentException("Unknown grade: " + borrowerGrade);
        }
        this.strategyType = this.interestStrategy.getClass().getSimpleName();
    }

    public InterestStrategy getInterestStrategy() {
        return interestStrategy;
    }

    public void submit(Borrower borrower, Money amount, Tenor tenor) {
        if (this.state != LoanState.SUBMITTED) {
            throw new IllegalStateException("Loan can only be submitted once and is in state: " + this.state);
        }

        Grade borrowerGrade = borrower.getCreditGrade();

        if (amount == null || amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount tidak valid");
        }
        if (amount.getAmount().compareTo(new BigDecimal("1000000")) <= 0) {
            throw new IllegalArgumentException("Amount kurang dari batas minimal");
        }

        if (amount.getAmount().compareTo(borrowerGrade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }

        if (tenor == null || !borrowerGrade.getAllowedTenors().contains(tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }

        this.amount = amount;
        this.tenor = tenor;
        determineInterestStrategy(borrowerGrade);
        this.state = LoanState.VALIDATED;
    }

    public void validate() {
        if (this.amount == null || this.amount.getAmount().compareTo(new java.math.BigDecimal("1000000")) <= 0) {
            throw new IllegalArgumentException("Amount harus > 1.000.000");
        }
        if (this.grade == null) {
            throw new IllegalStateException("Grade borrower belum ditentukan");
        }
        if (this.amount.getAmount().compareTo(this.grade.getMaxAmount().getAmount()) > 0) {
            throw new IllegalArgumentException("Amount melebihi limit grade");
        }
        if (this.tenor == null || !this.grade.getAllowedTenors().contains(this.tenor)) {
            throw new IllegalArgumentException("Tenor tidak tersedia untuk grade ini");
        }
    }

    public void startFunding() {
        if (this.state != LoanState.VALIDATED) {
            throw new IllegalStateException("Hanya bisa memulai funding jika status VALIDATED");
        }
        this.state = LoanState.FUNDING;
        this.fundingDeadline = LocalDate.now().plusDays(14);
        this.totalFunding = java.math.BigDecimal.ZERO;
    }

    public void addFunding(LenderId lenderId, Money amount, Lender lender) {
        if (this.fundingDeadline != null && LocalDate.now().isAfter(this.fundingDeadline)) {
            this.state = LoanState.CANCELLED;
            throw new IllegalStateException("Deadline terlewat");
        }

        if (amount.getAmount().compareTo(new java.math.BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Minimum kontribusi adalah Rp 100.000");
        }

        java.math.BigDecimal currentTotal = getTotalFunded().getAmount();
        java.math.BigDecimal targetAmount = this.amount.getAmount();
        java.math.BigDecimal remainingAmount = targetAmount.subtract(currentTotal);

        java.math.BigDecimal actualAmount = amount.getAmount();
        if (actualAmount.compareTo(remainingAmount) > 0) {
            actualAmount = remainingAmount;
        }

        double portion = actualAmount.doubleValue() / targetAmount.doubleValue();

        Funding funding = new Funding(
                new com.pq.domain.model.valueobject.FundingId("FND-" + System.nanoTime()),
                lenderId,
                new Money(actualAmount),
                portion);
        this.fundings.add(funding);
    }

    public void cancel(Borrower borrower, List<Lender> lenders) {
        if (this.state == LoanState.DISBURSED || this.state == LoanState.REPAYMENT || this.state == LoanState.CLOSED) {
            throw new IllegalStateException("Loan tidak dapat dibatalkan setelah dana cair");
        }

        Money totalFunded = getTotalFunded();
        if (totalFunded.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            this.state = LoanState.CANCELLED;
            return;
        }

        double percent = getFundingPercentage();
        java.math.BigDecimal rate = java.math.BigDecimal.ZERO;
        if (percent > 0 && percent <= 50) {
            rate = new java.math.BigDecimal("0.01");
        } else if (percent > 50 && percent < 100) {
            rate = new java.math.BigDecimal("0.02");
        }

        java.math.BigDecimal feeAmount = totalFunded.getAmount().multiply(rate).setScale(0,
                java.math.RoundingMode.HALF_UP);
        Money fee = new Money(feeAmount);

        if (borrower.getVirtualAccountBalance().getAmount().compareTo(feeAmount) < 0) {
            throw new IllegalStateException("Saldo tidak cukup untuk membayar denda");
        }

        if (feeAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            borrower.deductBalance(fee);
        }

        if (lenders != null) {
            for (Funding funding : fundings) {
                for (Lender lender : lenders) {
                    if (lender.getLenderId().getValue().equals(funding.getLenderId().getValue())) {
                        lender.addBalance(funding.getAmount());
                    }
                }
            }
        }

        this.state = LoanState.CANCELLED;
    }

    public void disburse() {
        if (getFundingPercentage() >= 100.0) {
            this.state = LoanState.DISBURSED;
            if (this.tenor != null && this.amount != null) {
                // Ensure strategy is determined if not already set
                if (this.interestStrategy == null && this.grade != null) {
                    determineInterestStrategy(this.grade);
                }
                
                if (this.interestStrategy == null) {
                    throw new IllegalStateException("Interest strategy must be determined before disbursement");
                }
                
                int months = this.tenor.getMonths();
                java.math.BigDecimal principalPerInstallment = this.amount.getAmount()
                        .divide(new java.math.BigDecimal(months), 0, java.math.RoundingMode.HALF_UP);
                double annualRate = 0.12; // Anda bisa mengambil rate ini berdasarkan grade/konfigurasi
                LocalDate startDate = LocalDate.now();

                List<Payment> generatedPayments = this.interestStrategy.generateSchedule(
                        this.amount,
                        this.tenor,
                        annualRate,
                        startDate);
                this.payments.addAll(generatedPayments);
                this.state = LoanState.REPAYMENT;
            }
        }
    }

    public void makeRepayment(PaymentId paymentId, List<Lender> lenders, Money amount) {
        // Validasi state loan
        if (this.state == LoanState.CLOSED) {
            throw new IllegalStateException("Loan sudah ditutup");
        }
        if (this.state != LoanState.REPAYMENT) {
            throw new IllegalStateException("Loan belum dalam masa repayment");
        }

        // 1. Cari cicilan berdasarkan paymentId
        Payment targetPayment = null;
        for (Payment payment : this.payments) {
            if (payment.getPaymentId().getValue().equals(paymentId.getValue())) {
                targetPayment = payment;
                break;
            }
        }

        if (targetPayment == null) {
            throw new IllegalArgumentException("Cicilan tidak ditemukan");
        }

        if (amount.getAmount().compareTo(targetPayment.getTotalAmount().getAmount()) != 0) {
            throw new IllegalArgumentException("Jumlah pembayaran tidak sesuai");
        }

        // Validasi apakah cicilan sudah dibayar
        if (targetPayment.getStatus() == com.pq.domain.model.enums.PaymentStatus.PAID) {
            throw new IllegalStateException("Tidak ada cicilan yang perlu dibayar");
        }

        // 2. Update status cicilan menjadi PAID dan catat tanggal
        targetPayment.markAsPaid();

        // 3. Distribusi dana ke lender secara proporsional
        java.math.BigDecimal amountToDistribute = targetPayment.getTotalAmount().getAmount();

        if (lenders != null) {
            for (Funding funding : this.fundings) {
                for (Lender lender : lenders) {
                    if (lender.getLenderId().getValue().equals(funding.getLenderId().getValue())) {
                        // Hitung bagian lender: jumlah cicilan * porsi lender
                        java.math.BigDecimal portionValue = java.math.BigDecimal.valueOf(funding.getPortion());
                        java.math.BigDecimal lenderShare = amountToDistribute.multiply(portionValue)
                                .setScale(0, java.math.RoundingMode.HALF_UP);

                        lender.addBalance(new Money(lenderShare));
                    }
                }
            }
        }

        // 4. Cek apakah ini cicilan terakhir untuk otomatisasi penutupan loan
        this.close();
    }

    public void close() {
        if (this.state == LoanState.CLOSED) {
            throw new IllegalStateException("Loan sudah ditutup");
        }

        // Penutupan hanya valid dilakukan saat masa REPAYMENT
        if (this.state != LoanState.REPAYMENT) {
            return;
        }

        // Cek apakah masih ada cicilan yang UNPAID
        boolean allPaid = true;
        for (Payment payment : this.payments) {
            if (payment.getStatus() == com.pq.domain.model.enums.PaymentStatus.UNPAID) {
                allPaid = false;
                break;
            }
        }

        // Jika semua sudah dibayar lunas, ubah status ke CLOSED
        if (allPaid) {
            this.state = LoanState.CLOSED;
        }
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setTenor(Tenor tenor) {
        this.tenor = tenor;
    }

    public void setState(LoanState state) {
        this.state = state;
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
        return state;
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