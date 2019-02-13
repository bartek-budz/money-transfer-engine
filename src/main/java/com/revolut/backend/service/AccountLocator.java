package com.revolut.backend.service;

import com.revolut.backend.domain.Account;

import java.util.Optional;

public interface AccountLocator {
    Optional<Account> getAccount(long id);
}
