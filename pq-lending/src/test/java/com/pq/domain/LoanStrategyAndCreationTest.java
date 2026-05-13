package com.pq.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LoanStrategyAndCreationTest {

    private Borrower borrowerA;
    private Borrower borrowerC;
    private Borrower borrowerD;

    @BeforeEach
    void setUp() {
        borrowerA = new Borrower(new BorrowerId("B-1"), "Borrower A", Grade.A, new Money(BigDecimal.valueOf(100000)));
        borrowerC = new Borrower(new BorrowerId("B-2"), "Borrower C", Grade.C, new Money(BigDecimal.valueOf(100000)));
        borrowerD = new Borrower(new BorrowerId("B-3"), "Borrower D", Grade.D, new Money(BigDecimal.valueOf(100000)));
    }

    @Test
    void testDetermineStrategy_GradeA_EffectiveRate() {
        Loan loan = new Loan(new LoanId("L-1"), borrowerA.getBorrowerId());
        loan.determineStrategy(borrowerA.getCreditGrade());
        assertTrue(loan.getInterestStrategy() instanceof EffectiveRateStrategy);
        assertEquals("EffectiveRateStrategy", loan.getStrategyType());
    }

    @Test
    void testDetermineStrategy_GradeC_FlatRate() {
        Loan loan = new Loan(new LoanId("L-2"), borrowerC.getBorrowerId());
        loan.determineStrategy(borrowerC.getCreditGrade());
        assertTrue(loan.getInterestStrategy() instanceof FlatRateStrategy);
        assertEquals("FlatRateStrategy", loan.getStrategyType());
    }

    @Test
    void testStrategyImmutable() {
        Loan loan = new Loan(new LoanId("L-3"), borrowerC.getBorrowerId());
        loan.determineStrategy(borrowerC.getCreditGrade());
        assertThrows(IllegalStateException.class, () -> loan.determineStrategy(borrowerA.getCreditGrade()));
    }

    @Test
    void testSubmit_Success() {
        Loan loan = new Loan(new LoanId("L-4"), borrowerC.getBorrowerId());
        // Tenor 3 bulan masuk batas validasi limit (Grade C limit 50 juta)
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.THREE);
        assertEquals(LoanState.VALIDATED, loan.getState());
        assertTrue(loan.getInterestStrategy() instanceof FlatRateStrategy);
    }

    @Test
    void testSubmit_CannotBeCalledTwice() {
        Loan loan = new Loan(new LoanId("L-4a"), borrowerC.getBorrowerId());
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.THREE);
        assertThrows(IllegalStateException.class, () ->
                loan.submit(borrowerC, new Money(BigDecimal.valueOf(20_000_000)), Tenor.SIX)
        );
    }

    @Test
    void testSubmit_Fail_AmountExceedsLimit() {
        Loan loan = new Loan(new LoanId("L-5"), borrowerC.getBorrowerId());
        assertThrows(IllegalArgumentException.class, () -> {
            // Grade C maksimal 50 juta, 100 juta harus ditolak
            loan.submit(borrowerC, new Money(BigDecimal.valueOf(100_000_000)), Tenor.THREE);
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
        loan.submit(borrowerC, new Money(BigDecimal.valueOf(30_000_000)), Tenor.THREE);
        loan.startFunding();
        assertEquals(LoanState.FUNDING, loan.getState());
        assertNotNull(loan.getFundingDeadline());
        assertTrue(loan.getFundingDeadline().isAfter(LocalDate.now()));
        assertEquals(0, loan.getTotalFunded().getAmount().intValue());
    }
}