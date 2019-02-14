package com.revolut.backend.service;

import com.revolut.backend.domain.Account;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class AccountLocator implements Serializable {

    protected final Map<Long, Account> accountById;

    public AccountLocator(Map<Long, Account> accountById) {
        this.accountById = accountById;
    }

    public Optional<Account> getAccount(long id) {
        return accountById.containsKey(id) ? Optional.of(accountById.get(id)) : Optional.empty();
    }

    public void validateAccountId(long id) {
        if (!accountById.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Account with id %d not found", id));
        }
    }
}
