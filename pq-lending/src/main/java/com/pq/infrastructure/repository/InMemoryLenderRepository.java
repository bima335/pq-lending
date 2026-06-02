package com.pq.infrastructure.repository;

import com.pq.domain.model.lender.Lender;
import com.pq.domain.model.valueobject.LenderId;
import com.pq.domain.repository.LenderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLenderRepository implements LenderRepository {
    
    private final Map<String, Lender> store = new HashMap<>();

    @Override
    public Optional<Lender> findById(LenderId lenderId) {
        return Optional.ofNullable(store.get(lenderId.getValue()));
    }

    @Override
    public void save(Lender lender) {
        store.put(lender.getLenderId().getValue(), lender);
    }
}