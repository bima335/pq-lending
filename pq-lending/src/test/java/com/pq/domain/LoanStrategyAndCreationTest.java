package com.pq.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanStrategyAndCreationTest {

    private Borrower borrowerA;
    private Borrower borrowerB;
    private Borrower borrowerC;
    private Borrower borrowerD;
    private Lender lender;

    @BeforeEach
    void setUp() {
        borrowerA = mock(Borrower.class);
        when(borrowerA.getBorrowerId()).thenReturn(new BorrowerId("B-1"));
        when(borrowerA.getCreditGrade()).thenReturn(Grade.A);
        when(borrowerA.getVirtualAccountBalance()).thenReturn(new Money(BigDecimal.valueOf(100000)));

        borrowerB = mock(Borrower.class);
        when(borrowerB.getBorrowerId()).thenReturn(new BorrowerId("B-4"));
        when(borrowerB.getCreditGrade()).thenReturn(Grade.B);
        when(borrowerB.getVirtualAccountBalance()).thenReturn(new Money(BigDecimal.valueOf(100000)));

        borrowerC = mock(Borrower.class);
        when(borrowerC.getBorrowerId()).thenReturn(new BorrowerId("B-2"));
        when(borrowerC.getCreditGrade()).thenReturn(Grade.C);
        when(borrowerC.getVirtualAccountBalance()).thenReturn(new Money(BigDecimal.valueOf(100000)));

        borrowerD = mock(Borrower.class);
        when(borrowerD.getBorrowerId()).thenReturn(new BorrowerId("B-3"));
        when(borrowerD.getCreditGrade()).thenReturn(Grade.D);
        when(borrowerD.getVirtualAccountBalance()).thenReturn(new Money(BigDecimal.valueOf(100000)));

        lender = mock(Lender.class);
        when(lender.getLenderId()).thenReturn(new LenderId("L-LENDER-1"));
    }

    @Test
    void testDetermineStrategy_GradeA_EffectiveRate() {
        Loan loan = new Loan(new LoanId("L-1"), borrowerA.getBorrowerId());
        loan.determineInterestStrategy(borrowerA.getCreditGrade());
        assertTrue(loan.getInterestStrategy() instanceof EffectiveRateStrategy);
        assertEquals("EffectiveRateStrategy", loan.getStrategyType());
    }

    @Test
    void testDetermineStrategy_GradeC_FlatRate() {
        Loan loan = new Loan(new LoanId("L-2"), borrowerC.getBorrowerId());
        loan.determineInterestStrategy(borrowerC.getCreditGrade());
        assertTrue(loan.getInterestStrategy() instanceof FlatRateStrategy);
        assertEquals("FlatRateStrategy", loan.getStrategyType());
    }

    @Test
    void testStrategyImmutable() {
        Loan loan = new Loan(new LoanId("L-3"), borrowerC.getBorrowerId());
        loan.determineInterestStrategy(borrowerC.getCreditGrade());
        assertThrows(IllegalStateException.class, () -> loan.determineInterestStrategy(borrowerA.getCreditGrade()));
    }

    @Test
    void testSubmit_Success() {
        Loan loan = new Loan(new LoanId("L-4"), borrowerC.getBorrowerId());
        // Tenor 6 bulan masuk batas validasi limit (Grade C limit 50 juta)
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.SIX);
        assertEquals(LoanState.VALIDATED, loan.getState());
        assertTrue(loan.getInterestStrategy() instanceof FlatRateStrategy);
    }

    @Test
    void testSubmit_CannotBeCalledTwice() {
        Loan loan = new Loan(new LoanId("L-4a"), borrowerC.getBorrowerId());
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.SIX);
        assertThrows(IllegalStateException.class,
                () -> loan.submit(borrowerC, new Money(BigDecimal.valueOf(20_000_000)), Tenor.SIX));
    }

    @Test
    void testSubmit_Fail_AmountExceedsLimit() {
        Loan loan = new Loan(new LoanId("L-5"), borrowerC.getBorrowerId());
        assertThrows(IllegalArgumentException.class, () -> {
            // Grade C maksimal 50 juta, 100 juta harus ditolak
            loan.submit(borrowerC, new Money(BigDecimal.valueOf(100_000_000)), Tenor.SIX);
        });
    }

    @Test
    void testSubmit_Fail_InvalidTenorForGrade() {
        Loan loan = new Loan(new LoanId("L-6"), borrowerD.getBorrowerId());
        assertThrows(IllegalArgumentException.class, () -> {
            // Grade D tidak diizinkan ambil tenor panjang (seperti 24 bulan)
            loan.submit(borrowerD, new Money(BigDecimal.valueOf(5_000_000)), null);
        });
    }

    @Test
    void testStartFunding() {
        Loan loan = new Loan(new LoanId("L-7"), borrowerC.getBorrowerId());
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.SIX);
        loan.startFunding();
        assertEquals(LoanState.FUNDING, loan.getState());
        assertNotNull(loan.getFundingDeadline());
        assertTrue(loan.getFundingDeadline().isAfter(LocalDate.now()));
        assertEquals(0, loan.getTotalFunded().getAmount().intValue());
    }

    @Test
    void testAnnualRate_GradeB_InterestFromGradeRate() {
        Loan loan = new Loan(new LoanId("L-RATE-B"), borrowerB.getBorrowerId());
        Money principal = new Money(BigDecimal.valueOf(12_000_000));

        // submit → VALIDATED, startFunding → FUNDING
        loan.submit(borrowerB, principal, Tenor.SIX);
        loan.startFunding();

        // addFunding 100%
        loan.addFunding(lender.getLenderId(), principal, lender);

        // disburse → generates payment schedule
        loan.disburse();

        List<Payment> payments = loan.getPayments();
        assertFalse(payments.isEmpty(), "Payment schedule harus ter-generate setelah disburse");

        // Bunga bulan 1 = pokok × rate / 12 = 12.000.000 × 0.15 / 12 = 150.000
        BigDecimal expectedInterest = BigDecimal.valueOf(150_000);
        assertEquals(0, expectedInterest.compareTo(payments.get(0).getInterest().getAmount()),
                "Grade B: bunga bulan 1 harus 150.000 (rate 15%), bukan " +
                        payments.get(0).getInterest().getAmount());
    }

    @Test
    void testAnnualRate_GradeC_InterestFromGradeRate() {
        Loan loan = new Loan(new LoanId("L-RATE-C"), borrowerC.getBorrowerId());
        Money principal = new Money(BigDecimal.valueOf(12_000_000));

        // submit → VALIDATED, startFunding → FUNDING
        loan.submit(borrowerC, principal, Tenor.SIX);
        loan.startFunding();

        // addFunding 100%
        loan.addFunding(lender.getLenderId(), principal, lender);

        // disburse → generates payment schedule
        loan.disburse();

        List<Payment> payments = loan.getPayments();
        assertFalse(payments.isEmpty(), "Payment schedule harus ter-generate setelah disburse");

        // Bunga bulan 1 = pokok × rate / 12 = 12.000.000 × 0.18 / 12 = 180.000
        BigDecimal expectedInterest = BigDecimal.valueOf(180_000);
        assertEquals(0, expectedInterest.compareTo(payments.get(0).getInterest().getAmount()),
                "Grade C: bunga bulan 1 harus 180.000 (rate 18%), bukan " +
                        payments.get(0).getInterest().getAmount());
    }

    @Test
    void testAnnualRate_GradeD_InterestFromGradeRate() {
        Loan loan = new Loan(new LoanId("L-RATE-D"), borrowerD.getBorrowerId());
        Money principal = new Money(BigDecimal.valueOf(6_000_000));

        // submit → VALIDATED, startFunding → FUNDING
        loan.submit(borrowerD, principal, Tenor.SIX);
        loan.startFunding();

        // addFunding 100%
        loan.addFunding(lender.getLenderId(), principal, lender);

        // disburse → generates payment schedule
        loan.disburse();

        List<Payment> payments = loan.getPayments();
        assertFalse(payments.isEmpty(), "Payment schedule harus ter-generate setelah disburse");

        // Bunga bulan 1 = pokok × rate / 12 = 6.000.000 × 0.24 / 12 = 120
        BigDecimal expectedInterest = BigDecimal.valueOf(120_000);
        assertEquals(0, expectedInterest.compareTo(payments.get(0).getInterest().getAmount()),
                "Grade D: bunga bulan 1 harus 120.000 (rate 24%), bukan " +
                        payments.get(0).getInterest().getAmount());
    }
}