package com.pq.domain.repository;

import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.LenderId;
import java.util.Optional;

public interface LenderRepository {
    Optional<Lender> findById(LenderId lenderId);
    
    void save(Lender lender);
}