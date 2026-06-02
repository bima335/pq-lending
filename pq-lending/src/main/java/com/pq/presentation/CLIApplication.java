package com.pq.presentation;

import com.pq.application.usecase.DisburseLoanUseCase;
import com.pq.application.usecase.FundLoanUseCase;
import com.pq.application.usecase.RepayLoanUseCase;
import com.pq.application.usecase.SubmitLoanUseCase;
import com.pq.application.usecase.ValidateLoanUseCase;
import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.enums.Grade;
import com.pq.domain.model.enums.LoanState;
import com.pq.domain.model.enums.Tenor;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.loan.Payment;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.repository.BorrowerRepository;
import com.pq.domain.repository.LenderRepository;
import com.pq.domain.repository.LoanRepository;
import com.pq.infrastructure.repository.InMemoryBorrowerRepository;
import com.pq.infrastructure.repository.InMemoryLenderRepository;
import com.pq.infrastructure.repository.InMemoryLoanRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class CLIApplication {
    private final Scanner scanner;
    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;
    private final SubmitLoanUseCase submitLoanUseCase;
    private final ValidateLoanUseCase validateLoanUseCase;
    private final FundLoanUseCase fundLoanUseCase;
    private final DisburseLoanUseCase disburseLoanUseCase;
    private final RepayLoanUseCase repayLoanUseCase;

    public CLIApplication() {
        this.scanner = new Scanner(System.in);
        this.loanRepository = new InMemoryLoanRepository();
        this.borrowerRepository = new InMemoryBorrowerRepository();
        this.lenderRepository = new InMemoryLenderRepository();
        this.submitLoanUseCase = new SubmitLoanUseCase(borrowerRepository, loanRepository);
        this.validateLoanUseCase = new ValidateLoanUseCase(loanRepository);
        this.fundLoanUseCase = new FundLoanUseCase(loanRepository, lenderRepository);
        this.disburseLoanUseCase = new DisburseLoanUseCase(loanRepository, borrowerRepository);
        this.repayLoanUseCase = new RepayLoanUseCase(loanRepository, borrowerRepository, lenderRepository);
    }

    public void run() {
        while (true) {
            printMainMenu();
            int option = askInt("Pilih menu: ");
            switch (option) {
                case 1:
                    borrowerMenu();
                    break;
                case 2:
                    lenderMenu();
                    break;
                case 3:
                    System.out.println("Keluar. Terima kasih.");
                    return;
                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("=== MENU UTAMA ===");
        System.out.println("1. Simulasi sebagai Borrower");
        System.out.println("2. Simulasi sebagai Lender");
        System.out.println("3. Exit");
    }

    private void borrowerMenu() {
        while (true) {
            System.out.println("=== MENU BORROWER ===");
            System.out.println("4. Daftarkan diri sebagai Borrower baru");
            System.out.println("5. Ajukan Pinjaman");
            System.out.println("6. Lihat Status Pinjaman");
            System.out.println("7. Bayar Cicilan");
            System.out.println("8. Kembali ke menu utama");

            int option = askInt("Pilih menu Borrower: ");
            switch (option) {
                case 4:
                    registerBorrower();
                    break;
                case 5:
                    submitLoan();
                    break;
                case 6:
                    viewLoanStatus();
                    break;
                case 7:
                    repayLoan();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }

    private void lenderMenu() {
        while (true) {
            System.out.println("=== MENU LENDER ===");
            System.out.println("9. Daftarkan diri sebagai Lender baru");
            System.out.println("10. Lihat daftar pinjaman yang tersedia untuk didanai");
            System.out.println("11. Dana Pinjaman");
            System.out.println("12. Kembali ke menu utama");

            int option = askInt("Pilih menu Lender: ");
            switch (option) {
                case 9:
                    registerLender();
                    break;
                case 10:
                    listAvailableFundingLoans();
                    break;
                case 11:
                    fundLoan();
                    break;
                case 12:
                    return;
                default:
                    System.out.println("Pilihan tidak valid. Silakan coba lagi.");
            }
        }
    }

    private void registerBorrower() {
        try {
            String name = askString("Nama borrower: ");
            String gradeInput = askString("Grade borrower (A/B/C/D): ");
            Grade grade = Grade.valueOf(gradeInput.trim().toUpperCase());
            BorrowerId borrowerId = new BorrowerId(UUID.randomUUID().toString());
            Borrower borrower = new Borrower(borrowerId, name, grade, new Money(BigDecimal.ZERO));
            borrowerRepository.save(borrower);
            System.out.println("Borrower berhasil didaftarkan. borrowerId=" + borrowerId.getValue());
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void submitLoan() {
        try {
            String borrowerIdStr = askString("BorrowerId: ");
            String amountInput = askString("Jumlah pinjaman (misal 1000000): ");
            String tenorInput = askString("Tenor dalam bulan (6, 12, 18, 24, 36): ");
            Money amount = parseMoney(amountInput);
            Tenor tenor = Tenor.fromMonths(Integer.parseInt(tenorInput.trim()));

            String loanId = submitLoanUseCase.execute(borrowerIdStr, amount, tenor);
            Borrower borrower = borrowerRepository.findById(new BorrowerId(borrowerIdStr))
                    .orElseThrow(() -> new IllegalArgumentException("Borrower tidak ditemukan setelah submit."));
            validateLoanUseCase.execute(loanId, borrower.getCreditGrade());
            System.out.println("Pinjaman berhasil diajukan. loanId=" + loanId);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void viewLoanStatus() {
        try {
            String loanIdStr = askString("LoanId: ");
            Loan loan = loanRepository.findById(new com.pq.domain.model.valueobject.LoanId(loanIdStr))
                    .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));

            System.out.println("LoanId: " + loan.getLoanId().getValue());
            System.out.println("State: " + loan.getState());
            System.out.println("Amount: " + loan.getAmount());
            System.out.println("Grade: " + (loan.getGrade() != null ? loan.getGrade() : "-"));
            System.out.println("Funding: " + String.format("%.2f%%", loan.getFundingPercentage()));

            if (loan.getState() == LoanState.REPAYMENT || !loan.getPayments().isEmpty()) {
                System.out.println("Daftar cicilan:");
                List<Payment> payments = loan.getPayments();
                if (payments.isEmpty()) {
                    System.out.println("  Tidak ada cicilan yang tersedia.");
                } else {
                    for (Payment payment : payments) {
                        System.out.println("  Cicilan " + payment.getInstallmentNumber() + ": total=" + payment.getTotalAmount() + ", status=" + payment.getStatus() + ", due=" + payment.getDueDate());
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void repayLoan() {
        try {
            String loanIdStr = askString("LoanId: ");
            String amountInput = askString("Jumlah bayar cicilan: ");
            Money amount = parseMoney(amountInput);
            repayLoanUseCase.execute(loanIdStr, amount);
            System.out.println("Pembayaran cicilan berhasil dicatat untuk loanId=" + loanIdStr);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void registerLender() {
        try {
            String name = askString("Nama lender: ");
            String balanceInput = askString("Saldo awal: ");
            Money balance = parseMoney(balanceInput);
            LenderId lenderId = new LenderId(UUID.randomUUID().toString());
            Lender lender = new Lender(lenderId, name, balance);
            lenderRepository.save(lender);
            System.out.println("Lender berhasil didaftarkan. lenderId=" + lenderId.getValue());
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void listAvailableFundingLoans() {
        List<Loan> fundingLoans = new ArrayList<>();
        for (Loan loan : loanRepository.findAll()) {
            if (loan.getState() == LoanState.FUNDING) {
                fundingLoans.add(loan);
            }
        }

        if (fundingLoans.isEmpty()) {
            System.out.println("Tidak ada pinjaman yang tersedia untuk didanai.");
            return;
        }

        System.out.println("Pinjaman yang tersedia untuk didanai:");
        for (Loan loan : fundingLoans) {
            System.out.println("  loanId=" + loan.getLoanId().getValue()
                    + ", amount=" + loan.getAmount()
                    + ", grade=" + loan.getGrade()
                    + ", funded=" + String.format("%.2f%%", loan.getFundingPercentage()));
        }
    }

    private void fundLoan() {
        try {
            String loanIdStr = askString("LoanId: ");
            String lenderIdStr = askString("LenderId: ");
            String amountInput = askString("Jumlah pendanaan: ");
            Money amount = parseMoney(amountInput);
            fundLoanUseCase.execute(loanIdStr, lenderIdStr, amount);
            System.out.println("Pendanaan berhasil untuk loanId=" + loanIdStr);

            Loan loan = loanRepository.findById(new com.pq.domain.model.valueobject.LoanId(loanIdStr))
                    .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan setelah pendanaan."));
            if (loan.getFundingPercentage() >= 100.0 && loan.getState() == LoanState.FUNDING) {
                disburseLoanUseCase.execute(loanIdStr);
                System.out.println("Pendanaan 100%. Loan otomatis di-disburse.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private String askString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int askInt(String prompt) {
        while (true) {
            try {
                String input = askString(prompt);
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Masukkan angka yang benar.");
            }
        }
    }

    private Money parseMoney(String input) {
        try {
            BigDecimal amount = new BigDecimal(input.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Jumlah harus lebih besar dari 0.");
            }
            return new Money(amount);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Format jumlah tidak valid.");
        }
    }
}
