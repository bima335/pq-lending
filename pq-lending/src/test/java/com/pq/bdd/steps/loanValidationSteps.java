package com.pq.bdd.steps;

import com.pq.domain.*;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;

import io.cucumber.java.en.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class loanValidationSteps {
    private Borrower borrower;
    private Exception exception;
    private Loan loan;

    // ===== GIVEN =====

    @Given("borrower dengan grade A")
    public void borrower_dengan_grade_a() {
        borrower = new Borrower(
                new BorrowerId("BRW-001"), "Budi",
                Grade.A, new Money(new BigDecimal("1000000")));
    }

    @Given("borrower dengan grade B")
    public void borrower_dengan_grade_b() {
        borrower = new Borrower(
                new BorrowerId("BRW-002"), "Siti",
                Grade.B, new Money(new BigDecimal("1000000")));
    }

    @Given("borrower dengan grade C")
    public void borrower_dengan_grade_c() {
        borrower = new Borrower(
                new BorrowerId("BRW-003"), "Andi",
                Grade.C, new Money(new BigDecimal("1000000")));
    }

    @Given("borrower dengan grade D")
    public void borrower_dengan_grade_d() {
        borrower = new Borrower(
                new BorrowerId("BRW-004"), "Rina",
                Grade.D, new Money(new BigDecimal("1000000")));
    }

    // Given untuk validasi amount yang general
    @Given("borrower mengajukan pinjaman")
    public void borrower_mengajukan_pinjaman() {
        // Grade A supaya limit tidak ikut campur
        borrower = new Borrower(
                new BorrowerId("BRW-001"), "Budi",
                Grade.A, new Money(new BigDecimal("1000000")));
    }

    // ===== WHEN =====

    @When("borrower mengajukan pinjaman sebesar {int}")
    public void borrower_mengajukan_pinjaman_sebesar(Integer amount) {
        try {
            loan = new Loan(
                    new LoanId("LOAN-001"),
                    borrower.getBorrowerId());
            loan.submit(
                    borrower,
                    new Money(new BigDecimal(amount)),
                    Tenor.THREE);
            exception = null;
        } catch (Exception e) {
            exception = e;
        }
    }

    @When("borrower mengajukan tenor {int} bulan")
    public void borrower_mengajukan_tenor_bulan(Integer bulan) {
        try {
            Tenor tenor = Tenor.fromMonths(bulan);
            loan = new Loan(
                    new LoanId("LOAN-001"),
                    borrower.getBorrowerId());
            loan.submit(
                    borrower,
                    new Money(new BigDecimal("5000000")),
                    tenor);
            exception = null;
        } catch (Exception e) {
            exception = e;
        }
    }

    // ===== THEN =====

    @Then("limit maksimal pinjamannya adalah {int}")
    public void limit_maksimal_pinjamannya_adalah(Integer expectedLimit) {
        assertEquals(
                new BigDecimal(expectedLimit),
                borrower.getCreditGrade().getMaxAmount().getAmount());
    }

    @Then("tenor yang diizinkan adalah {string}")
    public void tenor_yang_diizinkan_adalah(String tenorsStr) {
        String[] parts = tenorsStr.split(" ");
        List<Tenor> allowed = borrower.getCreditGrade()
                .getAllowedTenors();
        assertEquals(parts.length, allowed.size());
        for (String part : parts) {
            Tenor expected = Tenor.fromMonths(
                    Integer.parseInt(part));
            assertTrue(
                    allowed.contains(expected),
                    "Tenor " + part + " tidak ditemukan di grade ini");
        }
    }

    @Then("pengajuan ditolak dengan pesan {string}")
    public void pengajuan_ditolak_dengan_pesan(String pesan) {
        assertNotNull(exception,
                "Seharusnya ada exception tapi tidak ada");
        assertEquals(pesan, exception.getMessage());
    }

    @Then("pengajuan tidak ditolak karena amount sesuai dengan limit grade")
    public void pengajuan_tidak_ditolak_karena_amount_sesuai_dengan_limit_grade() {
        assertNull(exception,
                "Seharusnya tidak ada exception: "
                        + (exception != null ? exception.getMessage() : ""));
    }

    @Then("pengajuan tidak ditolak karena tenor")
    public void pengajuan_tidak_ditolak_karena_tenor() {
        assertNull(exception,
                "Seharusnya tidak ada exception: "
                        + (exception != null ? exception.getMessage() : ""));
    }
}
