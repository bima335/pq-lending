package com.p2plending.domain.model.lender;

import java.util.Optional;

public interface LenderRepository {
    Optional<Lender> findById(LenderId id);
    void save(Lender lender);
}