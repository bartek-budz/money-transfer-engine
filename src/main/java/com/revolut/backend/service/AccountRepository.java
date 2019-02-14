package com.revolut.backend.service;

import com.revolut.backend.api.AccountService;
import com.revolut.backend.domain.Account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AccountRepository extends AccountLocator implements AccountService, Serializable {
    private final AtomicLong idGenerator = new AtomicLong();

    public AccountRepository() {
        super(new ConcurrentHashMap<>());
    }

    @Override
    public long createAccount(BigDecimal initialBalance) {
        long id = idGenerator.getAndIncrement();
        Account account = new Account(initialBalance);
        accountById.put(id, account);
        return id;
    }

    @Override
    public BigDecimal checkBalance(long accountId) {
        validateAccountId(accountId);
        return accountById.get(accountId).getBalance();
    }
}
