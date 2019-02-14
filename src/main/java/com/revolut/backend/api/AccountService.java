package com.revolut.backend.api;

import java.math.BigDecimal;

public interface AccountService {

    default long createAccount() {
        return createAccount(BigDecimal.ZERO);
    }

    long createAccount(BigDecimal initialBalance);

    BigDecimal checkBalance(long accountId);
}
