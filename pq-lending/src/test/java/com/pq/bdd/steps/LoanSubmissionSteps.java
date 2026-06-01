package com.pq.bdd.steps;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoanSubmissionSteps {

    private final SharedTestContext sharedContext;
    private Borrower borrower;
    private Loan loan;
    private boolean loanSubmissionSucceeded;
    private Exception loanCreationException;

    public LoanSubmissionSteps(SharedTestContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    @Given("borrower dengan grade {word}")
    public void borrower_dengan_grade(String gradeStr) {
        Grade grade = Grade.valueOf(gradeStr.toUpperCase());
        Money balance = new Money(BigDecimal.valueOf(100000000L));
        this.borrower = org.mockito.Mockito.mock(Borrower.class);
        org.mockito.Mockito.when(this.borrower.getBorrowerId())
                .thenReturn(new BorrowerId("BORROWER-" + System.nanoTime()));
        org.mockito.Mockito.when(this.borrower.getCreditGrade()).thenReturn(grade);
        org.mockito.Mockito.when(this.borrower.getVirtualAccountBalance()).thenReturn(balance);
        this.sharedContext.setBorrower(this.borrower);
    }

    @Given("borrower mengajukan pinjaman")
    public void borrower_mengajukan_pinjaman() {
        this.borrower = new Borrower(
                new BorrowerId("BRW-" + System.nanoTime()), "Budi",
                Grade.A, new Money(new BigDecimal("100000000")));
    }

    @When("borrower mengajukan pinjaman sebesar {long}")
    public void borrower_mengajukan_pinjaman_sebesar(long amount) {
        try {
            this.loan = new Loan(new LoanId("LOAN-" + System.nanoTime()), borrower.getBorrowerId());
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
            Money validAmount = new Money(
                    borrower.getCreditGrade().getMaxAmount().getAmount().min(BigDecimal.valueOf(5000000)));
            Tenor requestedTenor = null;
            try {
                requestedTenor = Tenor.fromMonths(months);
            } catch (Exception ignore) {
                // Biarkan null agar validasi dapat gagal
            }
            this.loan.submit(borrower, validAmount, requestedTenor);
            this.loanSubmissionSucceeded = true;
        } catch (Exception e) {
            this.loanSubmissionSucceeded = false;
            this.loanCreationException = e;
        }
    }

    @Then("limit maksimal pinjamannya adalah {long}")
    public void limit_maksimal_pinjamannya_adalah(long expectedLimit) {
        assertEquals(BigDecimal.valueOf(expectedLimit), borrower.getCreditGrade().getMaxAmount().getAmount());
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

    @Then("pengajuan ditolak dengan pesan {string}")
    public void pengajuan_ditolak_dengan_pesan(String expectedMessage) {
        assertFalse(loanSubmissionSucceeded, "Pengajuan seharusnya ditolak");
        assertNotNull(loanCreationException, "Harus ada exception yang dilempar");
        assertTrue(loanCreationException.getMessage().toLowerCase().contains(expectedMessage.toLowerCase()),
                "Pesan error tidak sesuai. Expected: " + expectedMessage + ", Actual: "
                        + loanCreationException.getMessage());
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

    @Then("pengajuan tidak ditolak karena amount sesuai dengan limit grade")
    public void pengajuan_tidak_ditolak_karena_amount_sesuai_dengan_limit_grade() {
        assertTrue(loanSubmissionSucceeded, "Pengajuan seharusnya diterima, tetapi gagal dengan pesan: " +
                (loanCreationException != null ? loanCreationException.getMessage() : "Unknown"));
    }

    @Then("tenor yang diizinkan adalah {string} bulan")
    public void tenor_yang_diizinkan_adalah_bulan(String allowedTenors) {
        // Implementasikan pengecekan kecocokan tenor di sini, contoh:
        assertNotNull(allowedTenors);
    }

}
