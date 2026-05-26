package com.pq.domain.repository;

import com.pq.domain.model.borrower.Borrower;
import com.pq.domain.model.valueobject.BorrowerId;
import java.util.Optional;

public interface BorrowerRepository {
    Optional<Borrower> findById(BorrowerId borrowerId);
    
    void save(Borrower borrower);
}