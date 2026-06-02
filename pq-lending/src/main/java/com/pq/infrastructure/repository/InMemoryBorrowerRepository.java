package com.pq.infrastructure.repository;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.valueobject.BorrowerId;
import com.pq.domain.repository.BorrowerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryBorrowerRepository implements BorrowerRepository {
    
    // Menggunakan String (value dari ID) sebagai key agar aman
    private final Map<String, Borrower> store = new HashMap<>();

    @Override
    public Optional<Borrower> findById(BorrowerId borrowerId) {
        return Optional.ofNullable(store.get(borrowerId.getValue()));
    }

    @Override
    public void save(Borrower borrower) {
        store.put(borrower.getBorrowerId().getValue(), borrower);
    }
}