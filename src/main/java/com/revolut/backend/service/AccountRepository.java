package com.revolut.backend.service;

import com.revolut.backend.api.AccountService;
import com.revolut.backend.domain.Account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AccountRepository implements AccountService, AccountLocator, Serializable {
    private final AtomicLong idGenerator = new AtomicLong();
    private final ConcurrentHashMap<Long, Account> accountById = new ConcurrentHashMap<>();

    @Override
    public long createAccount(BigDecimal initialBalance) {
        long id = idGenerator.getAndIncrement();
        Account account = new Account(initialBalance);
        accountById.put(id, account);
        return id;
    }

    @Override
    public BigDecimal checkBalance(long id) {
        return getAccount(id).orElseThrow(IllegalArgumentException::new).getBalance();
    }

    @Override
    public Optional<Account> getAccount(long id) {
        return accountById.containsKey(id) ? Optional.of(accountById.get(id)) : Optional.empty();
    }
}
