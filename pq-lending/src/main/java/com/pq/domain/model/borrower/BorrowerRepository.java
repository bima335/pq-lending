package com.p2plending.domain.model.borrower;

import java.util.Optional;

public interface BorrowerRepository {
    Optional<Borrower> findById(BorrowerId id);
    void save(Borrower borrower);
}