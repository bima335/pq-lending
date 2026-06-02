package com.pq.application.usecase;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.loan.Funding;
import com.pq.domain.model.loan.Loan;
import com.pq.domain.model.valueobject.LoanId;
import com.pq.domain.model.valueobject.Money;
import com.pq.domain.model.valueobject.PaymentId; // Asumsi Anda sudah membuat class ini
import com.pq.domain.repository.BorrowerRepository;
import com.pq.domain.repository.LenderRepository;
import com.pq.domain.repository.LoanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepayLoanUseCase {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;

    public RepayLoanUseCase(LoanRepository loanRepository, 
                            BorrowerRepository borrowerRepository, 
                            LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.lenderRepository = lenderRepository;
    }

    public void execute(String loanIdStr, Money repaymentAmount) {
        LoanId loanId = new LoanId(loanIdStr);

        // 1. Ambil data Loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman tidak ditemukan."));

        // 2. Ambil data Borrower berdasarkan ID yang ada di dalam Loan
        Borrower borrower = borrowerRepository.findById(loan.getBorrowerId())
                .orElseThrow(() -> new IllegalStateException("Data Peminjam tidak valid."));

        // 3. Potong saldo Borrower (Menggunakan fungsi deductBalance yang baru Anda buat)
        // Jika saldo tidak cukup, fungsi ini akan otomatis melempar IllegalStateException
        borrower.deductBalance(repaymentAmount);

        // 4. Kumpulkan daftar Lender yang berhak menerima pengembalian dana
        // Kita ambil dari daftar "Funding" yang ada di dalam entitas Loan
        List<Lender> lenders = new ArrayList<>();
        for (Funding funding : loan.getFundings()) {
            Lender lender = lenderRepository.findById(funding.getLenderId())
                    .orElseThrow(() -> new IllegalStateException("Data Lender tidak ditemukan."));
            lenders.add(lender);
        }

        // 5. Buat ID Pembayaran baru
        PaymentId paymentId = new PaymentId(UUID.randomUUID().toString());

        // 6. Eksekusi Domain: Catat pembayaran & distribusikan dana
        // Asumsi: Logika penambahan saldo ke masing-masing Lender (addBalance) 
        // terjadi di dalam class state (RepaymentState) saat makeRepayment dipanggil.
        loan.makeRepayment(paymentId, lenders, repaymentAmount);

        // 7. Simpan semua perubahan ke dalam Repository
        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        
        for (Lender lender : lenders) {
            lenderRepository.save(lender);
        }
    }
}