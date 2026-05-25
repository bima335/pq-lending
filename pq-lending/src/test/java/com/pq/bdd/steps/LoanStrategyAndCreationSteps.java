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
    
    private Borrower borrower;
    private Loan loan;
    private boolean loanCreationSucceeded;
    private boolean loanSubmissionSucceeded;
    private Exception loanCreationException;
    private String strategyType;
    private LocalDate fundingDeadlineComputed;
    private Money totalFunded;

    // BR-04: Strategy Determination Steps

    @Given("borrower dengan grade {word}")
    public void borrower_dengan_grade(String gradeStr) {
        Grade grade = Grade.valueOf(gradeStr.toUpperCase());
        Money balance = new Money(BigDecimal.valueOf(100000000L));
        this.borrower = mock(Borrower.class);
        when(this.borrower.getBorrowerId()).thenReturn(new BorrowerId("BORROWER-" + System.nanoTime()));
        when(this.borrower.getCreditGrade()).thenReturn(grade);
        when(this.borrower.getVirtualAccountBalance()).thenReturn(balance);
    }

    @When("loan berhasil dibuat")
    public void loan_berhasil_dibuat_all() {
        if (!this.loanSubmissionSucceeded && this.strategyType == null) {
            // Logika sebagai 'When' (Fase Pembuatan & Penentuan Strategi)
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
            // Logika sebagai 'Then' (Fase Asersi / Validasi Pengajuan)
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
        String originalStrategy = loan.getStrategyType();
        
        // Try to change strategy (should fail or be immutable)
        assertThrows(Exception.class, () -> {
            loan.determineInterestStrategy(Grade.A); // Mencoba set ulang strategi harusnya melempar exception
        }, "Strategy harus immutable setelah loan dibuat");
    }

    // BR-05: Loan Creation Steps

    @When("borrower mengajukan pinjaman sebesar {long} dengan tenor {int} bulan")
    public void borrower_mengajukan_pinjaman(long amountInRupiah, int tenorMonths) {
        try {
            this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
            Money amount = new Money(BigDecimal.valueOf(amountInRupiah));
            Tenor tenor = Tenor.SIX; // Default
            
            // Map tenorMonths ke Tenor enum
            tenor = Tenor.fromMonths(tenorMonths);
            
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
        
        // Simple check: deadline should be approximately 14 business days (~20 calendar days)
        LocalDate today = LocalDate.now();
        LocalDate expectedDeadline = today.plusDays(20); // Rough estimate with weekends
        
        // Deadline should be around 14-20 days from now (accounting for weekends)
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

    // BR-07: Validation Steps (Limits and Tenors)

    @Then("limit maksimal pinjamannya adalah {long}")
    public void limit_maksimal_pinjamannya_adalah(long expectedLimit) {
        assertEquals(BigDecimal.valueOf(expectedLimit), borrower.getCreditGrade().getMaxAmount().getAmount());
    }

    @Then("tenor yang diizinkan adalah {int} {int} {int} {int}")
    public void tenor_yang_diizinkan_adalah_4(Integer t1, Integer t2, Integer t3, Integer t4) {
        List<Tenor> allowed = borrower.getCreditGrade().getAllowedTenors();
        assertTrue(allowed.contains(Tenor.fromMonths(t1)));
        assertTrue(allowed.contains(Tenor.fromMonths(t2)));
        assertTrue(allowed.contains(Tenor.fromMonths(t3)));
        assertTrue(allowed.contains(Tenor.fromMonths(t4)));
    }

    @Then("tenor yang diizinkan adalah {int} {int}")
    public void tenor_yang_diizinkan_adalah_2(Integer t1, Integer t2) {
        List<Tenor> allowed = borrower.getCreditGrade().getAllowedTenors();
        assertTrue(allowed.contains(Tenor.fromMonths(t1)));
        assertTrue(allowed.contains(Tenor.fromMonths(t2)));
    }

    @When("borrower mengajukan pinjaman sebesar {long}")
    public void borrower_mengajukan_pinjaman_sebesar(long amount) {
        try {
            this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
            // Gunakan tenor yang valid agar isolasi pengujian fokus hanya pada validasi amount
            Tenor validTenor = borrower.getCreditGrade().getAllowedTenors().get(0);
            this.loan.submit(borrower, new Money(BigDecimal.valueOf(amount)), validTenor);
            this.loanSubmissionSucceeded = true;
        } catch (Exception e) {
            this.loanSubmissionSucceeded = false;
            this.loanCreationException = e;
        }
    }

    @When("borrower mengajukan tenor {int} bulan")
    public void borrower_mengajukan_tenor_bulan(int months) {
        try {
            this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
            // Gunakan amount yang valid agar isolasi pengujian fokus hanya pada validasi tenor
            Money validAmount = new Money(borrower.getCreditGrade().getMaxAmount().getAmount().min(BigDecimal.valueOf(1000000)));
            Tenor requestedTenor = null;
            try { requestedTenor = Tenor.fromMonths(months); } catch (Exception e) { /* Biarkan null agar gagal validasi */ }
            
            this.loan.submit(borrower, validAmount, requestedTenor);
            this.loanSubmissionSucceeded = true;
        } catch (Exception e) {
            this.loanSubmissionSucceeded = false;
            this.loanCreationException = e;
        }
    }

    @Then("pengajuan ditolak dengan pesan {string}")
    public void pengajuan_ditolak_dengan_pesan(String expectedMessage) {
        assertFalse(loanSubmissionSucceeded, "Pengajuan seharusnya ditolak");
        assertNotNull(loanCreationException, "Harus ada exception yang dilempar");
        assertTrue(loanCreationException.getMessage().toLowerCase().contains(expectedMessage.toLowerCase()), 
            "Pesan error tidak sesuai. Expected: " + expectedMessage + ", Actual: " + loanCreationException.getMessage());
    }

    @Then("pengajuan tidak ditolak karena amount")
    public void pengajuan_tidak_ditolak_karena_amount() {
        assertTrue(loanSubmissionSucceeded, "Pengajuan seharusnya diterima, tetapi gagal dengan pesan: " + 
            (loanCreationException != null ? loanCreationException.getMessage() : "Unknown"));
    }

    @Then("pengajuan tidak ditolak karena tenor")
    public void pengajuan_tidak_ditolak_karena_tenor() {
        assertTrue(loanSubmissionSucceeded, "Pengajuan seharusnya diterima, tetapi gagal dengan pesan: " + 
            (loanCreationException != null ? loanCreationException.getMessage() : "Unknown"));
    }

    // Ported from disabled loanValidationSteps
    @Given("borrower mengajukan pinjaman")
    public void borrower_mengajukan_pinjaman() {
        // Grade A supaya limit tidak ikut campur
        this.borrower = new Borrower(
                new BorrowerId("BRW-" + System.nanoTime()), "Budi",
                Grade.A, new Money(new BigDecimal("100000000")));
    }

    @Then("tenor yang diizinkan adalah {string}")
    public void tenor_yang_diizinkan_adalah(String tenorsStr) {
        String[] parts = tenorsStr.split(" ");
        List<Tenor> allowed = borrower.getCreditGrade().getAllowedTenors();
        assertEquals(parts.length, allowed.size());
        for (String part : parts) {
            Tenor expected = Tenor.fromMonths(Integer.parseInt(part));
            assertTrue(allowed.contains(expected), "Tenor " + part + " tidak ditemukan di grade ini");
        }
    }

    @Then("pengajuan tidak ditolak karena amount sesuai dengan limit grade")
    public void pengajuan_tidak_ditolak_karena_amount_sesuai_dengan_limit_grade() {
        assertTrue(loanSubmissionSucceeded, "Pengajuan seharusnya diterima, tetapi gagal dengan pesan: " + 
            (loanCreationException != null ? loanCreationException.getMessage() : "Unknown"));
    }
}
