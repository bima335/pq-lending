package com.pq.domain;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loan Validation Tests")
public class LoanValidationTest {

    // =========================================================
    // Helper
    // =========================================================

    private Borrower buildBorrower(String id, String name, Grade grade) {
        return new Borrower(
                new BorrowerId(id),
                name,
                grade,
                new Money(new BigDecimal("1000000")));
    }

    private Loan buildLoan(String loanId, Borrower borrower) {
        return new Loan(new LoanId(loanId), borrower.getBorrowerId());
    }

    // =========================================================
    // BR-01 — Ketentuan Grade: Limit Amount
    // =========================================================

    @Nested
    @DisplayName("BR-01: Limit Amount per Grade")
    class GradeLimitTest {

        @Test
        @DisplayName("Grade A memiliki limit Rp 500.000.000")
        void gradeA_limitRp500Juta() {
            Borrower borrower = buildBorrower("BRW-001", "Budi", Grade.A);
            assertEquals(
                    new BigDecimal("500000000"),
                    borrower.getCreditGrade().getMaxAmount().getAmount());
        }

        @Test
        @DisplayName("Grade B memiliki limit Rp 200.000.000")
        void gradeB_limitRp200Juta() {
            Borrower borrower = buildBorrower("BRW-002", "Siti", Grade.B);
            assertEquals(
                    new BigDecimal("200000000"),
                    borrower.getCreditGrade().getMaxAmount().getAmount());
        }

        @Test
        @DisplayName("Grade C memiliki limit Rp 50.000.000")
        void gradeC_limitRp50Juta() {
            Borrower borrower = buildBorrower("BRW-003", "Andi", Grade.C);
            assertEquals(
                    new BigDecimal("50000000"),
                    borrower.getCreditGrade().getMaxAmount().getAmount());
        }

        @Test
        @DisplayName("Grade D memiliki limit Rp 10.000.000")
        void gradeD_limitRp10Juta() {
            Borrower borrower = buildBorrower("BRW-004", "Rina", Grade.D);
            assertEquals(
                    new BigDecimal("10000000"),
                    borrower.getCreditGrade().getMaxAmount().getAmount());
        }
    }

    // =========================================================
    // BR-01 — Ketentuan Grade: Tenor yang Diizinkan
    // =========================================================

    @Nested
    @DisplayName("BR-01: Tenor yang Diizinkan per Grade")
    class AllowedTenorTest {

        @Test
        @DisplayName("Grade A mengizinkan tenor 6, 12, 18, 24, dan 36 bulan")
        void gradeA_allowedTenors() {
            Borrower borrower = buildBorrower("BRW-001", "Budi", Grade.A);
            List<Tenor> allowed = borrower.getCreditGrade().getAllowedTenors();

            assertEquals(5, allowed.size());
            assertTrue(allowed.contains(Tenor.SIX));
            assertTrue(allowed.contains(Tenor.TWELVE));
            assertTrue(allowed.contains(Tenor.EIGHTEEN));
            assertTrue(allowed.contains(Tenor.TWENTY_FOUR));
            assertTrue(allowed.contains(Tenor.THIRTY_SIX));
        }

        @Test
        @DisplayName("Grade D hanya mengizinkan tenor 6 dan 12 bulan")
        void gradeD_allowedTenors() {
            Borrower borrower = buildBorrower("BRW-004", "Rina", Grade.D);
            List<Tenor> allowed = borrower.getCreditGrade().getAllowedTenors();

            assertEquals(2, allowed.size());
            assertTrue(allowed.contains(Tenor.TWELVE));
            assertTrue(allowed.contains(Tenor.SIX));
        }
    }

    // =========================================================
    // BR-02 — Validasi Amount
    // =========================================================

    @Nested
    @DisplayName("BR-02: Validasi Amount Pengajuan")
    class AmountValidationTest {

        @Test
        @DisplayName("Pengajuan ditolak jika amount <= 1.000.000")
        void submit_amountNol_throwsException() {
            Borrower borrower = buildBorrower("BRW-001", "Budi", Grade.A);
            Loan loan = buildLoan("LOAN-001", borrower);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> loan.submit(borrower, new Money(new BigDecimal("1000000")), Tenor.SIX));
            assertEquals("Amount harus > 1.000.000", ex.getMessage());
        }

        @Test
        @DisplayName("Pengajuan ditolak jika amount negatif")
        void submit_amountNegatif_throwsException() {
            Borrower borrower = buildBorrower("BRW-001", "Budi", Grade.A);
            Loan loan = buildLoan("LOAN-001", borrower);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> loan.submit(borrower, new Money(new BigDecimal("-1000000")), Tenor.SIX));
            assertEquals("Amount harus > 1.000.000", ex.getMessage());
        }

        @Test
        @DisplayName("Pengajuan ditolak jika amount melebihi limit grade")
        void submit_amountMelebihiLimitGrade_throwsException() {
            // Grade C limit = 50 juta, mengajukan 100 juta
            Borrower borrower = buildBorrower("BRW-003", "Andi", Grade.C);
            Loan loan = buildLoan("LOAN-001", borrower);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> loan.submit(borrower, new Money(new BigDecimal("100000000")), Tenor.SIX));
            assertEquals("Amount melebihi limit grade", ex.getMessage());
        }

        @Test
        @DisplayName("Pengajuan diterima jika amount dalam batas grade")
        void submit_amountDalamBatasGrade_berhasil() {
            // Grade C limit = 50 juta, mengajukan 30 juta
            Borrower borrower = buildBorrower("BRW-003", "Andi", Grade.C);
            Loan loan = buildLoan("LOAN-001", borrower);

            assertDoesNotThrow(
                    () -> loan.submit(borrower, new Money(new BigDecimal("30000000")), Tenor.SIX));
        }
    }

    // =========================================================
    // BR-03 — Validasi Tenor
    // =========================================================

    @Nested
    @DisplayName("BR-03: Validasi Tenor Pengajuan")
    class TenorValidationTest {

        @Test
        @DisplayName("Pengajuan ditolak jika tenor tidak tersedia untuk grade")
        void submit_tenorTidakTersedia_throwsException() {
            // Grade D hanya mengizinkan tenor 6 & 12, mengajukan tenor 18
            Borrower borrower = buildBorrower("BRW-004", "Rina", Grade.D);
            Loan loan = buildLoan("LOAN-001", borrower);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> loan.submit(borrower, new Money(new BigDecimal("5000000")), Tenor.EIGHTEEN));
            assertEquals("Tenor tidak tersedia untuk grade ini", ex.getMessage());
        }

        @Test
        @DisplayName("Pengajuan diterima jika tenor tersedia untuk grade")
        void submit_tenorTersedia_berhasil() {
            // Grade D mengizinkan tenor 6 & 12
            Borrower borrower = buildBorrower("BRW-004", "Rina", Grade.D);
            Loan loan = buildLoan("LOAN-001", borrower);

            assertDoesNotThrow(
                    () -> loan.submit(borrower, new Money(new BigDecimal("5000000")), Tenor.SIX));
        }
    }
}