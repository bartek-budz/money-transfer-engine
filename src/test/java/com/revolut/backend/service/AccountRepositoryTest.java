package com.revolut.backend.service;

import com.revolut.backend.domain.Account;
import io.netty.util.internal.ConcurrentSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.revolut.backend.utils.TestUtils.runConcurrently;
import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AccountRepository();
    }

    @Test
    void createAccountShouldReturnUniqueIdForEachAccount() {
        final int numOfConcurrentRequests = 100;
        //when
        Set<Long> accountIds = createAccountsConcurrently(numOfConcurrentRequests);
        //then
        assertEquals(numOfConcurrentRequests, accountIds.size());
    }

    @Test
    void checkBalanceShouldThrowExceptionForUnknownAccount() {
        assertThrows(IllegalArgumentException.class, () -> repository.checkBalance(999));
    }

    @Test
    void getAccountShouldAllowAccessingAccountById() {
        final int numOfAccounts = 50;
        //given
        Set<Long> accountIds = createAccountsConcurrently(numOfAccounts);
        //when
        Set<Account> accounts = retrieveAccountsConcurrently(accountIds);
        //then
        assertEquals(numOfAccounts, accounts.size());
    }

    @Test
    void getAccountShouldReturnEmptyForNotFoundAccount() {
        //when
        Optional<Account> account = repository.getAccount(99999);
        //then
        assertFalse(account.isPresent());
    }

    private Set<Long> createAccountsConcurrently(int howMany) {
        ConcurrentSet<Long> ids = new ConcurrentSet<>();
        List<Runnable> concurrentOperations = IntStream.range(0, howMany).boxed()
                .map(threadNumber -> (Runnable) () -> ids.add(repository.createAccount()))
                .collect(Collectors.toList());
        runConcurrently(concurrentOperations);
        return ids;
    }

    private Set<Account> retrieveAccountsConcurrently(Set<Long> accountIds) {
        Set<Account> accounts = new ConcurrentSet<>();
        List<Runnable> concurrentOperations = accountIds.stream()
                .map(accountId -> (Runnable) () -> repository.getAccount(accountId).ifPresent(accounts::add))
                .collect(Collectors.toList());
        runConcurrently(concurrentOperations);
        return accounts;
    }
}