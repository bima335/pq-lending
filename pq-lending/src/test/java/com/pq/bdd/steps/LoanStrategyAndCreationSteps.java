package com.pq.bdd.steps;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.strategy.InterestStrategy;
import com.pq.domain.model.loan.strategy.EffectiveRateStrategy;
import com.pq.domain.model.loan.strategy.FlatRateStrategy;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanStrategyAndCreationSteps {

    private final SharedTestContext sharedContext;
    private Borrower borrower;
    private Loan loan;
    private boolean loanCreationSucceeded;
    private boolean loanSubmissionSucceeded;
    private Exception loanCreationException;
    private String strategyType;

    public LoanStrategyAndCreationSteps(SharedTestContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    private void ensureBorrower() {
        if (this.borrower == null && this.sharedContext.getBorrower() != null) {
            this.borrower = this.sharedContext.getBorrower();
        }
    }

    // BR-04: Strategy Determination Steps

    @When("loan berhasil dibuat")
    public void loan_berhasil_dibuat_all() {
        ensureBorrower();
        if (!this.loanSubmissionSucceeded && this.strategyType == null) {
            try {
                this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
                this.loan.determineInterestStrategy(borrower.getCreditGrade());
                this.loanCreationSucceeded = true;
                this.strategyType = this.loan.getStrategyType();
            } catch (Exception e) {
                this.loanCreationSucceeded = false;
                this.loanCreationException = e;
            }
        } else {
            assertTrue(loanSubmissionSucceeded, "Loan submission should succeed");
            assertNotNull(loan);
            assertNotNull(loan.getAmount());
        }
    }

    @Then("strategy bunga yang digunakan adalah EffectiveRateStrategy")
    public void strategy_adalah_effective_rate() {
        assertTrue(loanCreationSucceeded, "Loan creation failed");
        assertEquals("EffectiveRateStrategy", strategyType, "Strategy type mismatch");
        assertNotNull(loan.getInterestStrategy());
        assertTrue(loan.getInterestStrategy() instanceof EffectiveRateStrategy);
    }

    @Then("strategy bunga yang digunakan adalah FlatRateStrategy")
    public void strategy_adalah_flat_rate() {
        assertTrue(loanCreationSucceeded, "Loan creation failed");
        assertEquals("FlatRateStrategy", strategyType, "Strategy type mismatch");
        assertNotNull(loan.getInterestStrategy());
        assertTrue(loan.getInterestStrategy() instanceof FlatRateStrategy);
    }

    @Then("strategy tidak bisa diubah setelah loan dibuat")
    public void strategy_tidak_bisa_diubah() {
        assertTrue(loanCreationSucceeded);

        assertThrows(Exception.class, () -> {
            loan.determineInterestStrategy(Grade.A);
        }, "Strategy harus immutable setelah loan dibuat");
    }

    // BR-05: Loan Creation Steps

    @When("borrower mengajukan pinjaman sebesar {long} dengan tenor {int} bulan")
    public void borrower_mengajukan_pinjaman(long amountInRupiah, int tenorMonths) {
        try {
            this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
            Money amount = new Money(BigDecimal.valueOf(amountInRupiah));
            Tenor tenor = Tenor.fromMonths(tenorMonths);

            this.loan.submit(borrower, amount, tenor);
            this.loanSubmissionSucceeded = true;
        } catch (Exception e) {
            this.loanSubmissionSucceeded = false;
            this.loanCreationException = e;
        }
    }

    @Then("status loan adalah VALIDATED")
    public void status_loan_adalah_validated() {
        assertTrue(loanSubmissionSucceeded);
        assertEquals(LoanState.VALIDATED, loan.getState(), "Loan status should be VALIDATED");
    }

    @Then("loan tidak berhasil dibuat")
    public void loan_tidak_berhasil_dibuat() {
        assertFalse(loanSubmissionSucceeded, "Loan submission should fail");
        assertNotNull(loanCreationException, "Exception should be thrown");
    }

    // BR-06: Funding Phase Steps

    @Given("loan dengan status VALIDATED")
    public void loan_dengan_status_validated() {
        Money balance = new Money(BigDecimal.valueOf(100000000L));
        this.borrower = mock(Borrower.class);
        when(this.borrower.getBorrowerId()).thenReturn(new BorrowerId("BORROWER-" + System.nanoTime()));
        when(this.borrower.getCreditGrade()).thenReturn(Grade.C);
        when(this.borrower.getVirtualAccountBalance()).thenReturn(balance);

        this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
        Money amount = new Money(BigDecimal.valueOf(30000000L));
        Tenor tenor = Tenor.SIX;

        this.loan.submit(borrower, amount, tenor);
        assertEquals(LoanState.VALIDATED, loan.getState());
    }

    @When("sistem memulai fase funding")
    public void sistem_memulai_fase_funding() {
        this.loan.startFunding();
    }

    @Then("status loan berubah menjadi FUNDING")
    public void status_loan_berubah_menjadi_funding() {
        assertEquals(LoanState.FUNDING, loan.getState(), "Loan status should be FUNDING");
    }

    @Then("funding deadline adalah 14 hari kerja dari sekarang")
    public void funding_deadline_adalah_14_hari_kerja() {
        LocalDate deadline = loan.getFundingDeadline();
        assertNotNull(deadline, "Funding deadline should not be null");

        LocalDate today = LocalDate.now();
        LocalDate expectedDeadline = today.plusDays(20);

        assertTrue(deadline.isAfter(today), "Deadline should be in the future");
        assertTrue(deadline.isBefore(expectedDeadline.plusDays(5)), "Deadline should be within reasonable range");
    }

    @Given("loan baru masuk fase FUNDING")
    public void loan_baru_masuk_fase_funding() {
        Money balance = new Money(BigDecimal.valueOf(100000000L));
        this.borrower = mock(Borrower.class);
        when(this.borrower.getBorrowerId()).thenReturn(new BorrowerId("BORROWER-" + System.nanoTime()));
        when(this.borrower.getCreditGrade()).thenReturn(Grade.A);
        when(this.borrower.getVirtualAccountBalance()).thenReturn(balance);

        this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
        Money amount = new Money(BigDecimal.valueOf(50000000L));
        Tenor tenor = Tenor.SIX;

        this.loan.submit(borrower, amount, tenor);
        this.loan.startFunding();
        assertEquals(LoanState.FUNDING, loan.getState());
    }

    @Then("total dana terkumpul adalah 0")
    public void total_dana_terkumpul_adalah_nol() {
        Money totalFunded = loan.getTotalFunded();
        assertNotNull(totalFunded);
        assertEquals(BigDecimal.ZERO, totalFunded.getAmount(), "Total funded should be zero");
    }
}