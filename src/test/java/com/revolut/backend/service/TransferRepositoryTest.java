package com.revolut.backend.service;

import com.revolut.backend.domain.TransferStatus;
import com.revolut.backend.domain.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.revolut.backend.domain.TransferStatus.DENIED_LACK_OF_FUNDS;
import static com.revolut.backend.domain.TransferStatus.SUCCESS;
import static com.revolut.backend.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferRepositoryTest {

    private TransferRepository repository;
    private Map<Long, Account> accounts;

    @BeforeEach
    void setUp() {
        accounts = new HashMap<>();
        AccountLocator accountLocator = new AccountLocator(accounts);
        repository = new TransferRepository(accountLocator);
    }

    @Test
    void concurrentTransfersShouldUpdateBalancesRespectively() {
        //given
        Account account1 = account(101L, amount(1000));
        Account account2 = account(999L, amount(999));
        //when
        Map<TransferStatus, AtomicInteger> counters = makeTransfersConcurrently(101L, 999L, 50, deterministicBalanceGenerator());
        //then
        Assertions.assertEquals(50, counters.get(SUCCESS).get());
        assertEquals(amount(749.44), account1.getBalance());
        assertEquals(amount(1249.56), account2.getBalance());
    }

    @Test
    void only1shouldPassRestShouldFailDueToLackOfFunds() {
        //given
        account(1L, amount(100));
        account(2L, amount(0));
        //when
        Map<TransferStatus, AtomicInteger> counters = makeTransfersConcurrently(1L, 2L, 10, i -> amount(100));
        //then
        assertTrue(counters.containsKey(SUCCESS));
        Assertions.assertEquals(1, counters.get(SUCCESS).get());
        assertTrue(counters.containsKey(DENIED_LACK_OF_FUNDS));
        Assertions.assertEquals(9, counters.get(DENIED_LACK_OF_FUNDS).get());
    }

    private Map<TransferStatus, AtomicInteger> makeTransfersConcurrently(long account1, long account2, int numConcurrent, Function<Integer, BigDecimal> amountGenerator) {
        Map<TransferStatus, AtomicInteger> counters = new ConcurrentHashMap<>();
        List<Runnable> concurrentTransfers = IntStream.range(0, numConcurrent).boxed()
                .map(threadNumber -> (Runnable) () -> {
                    long senderId;
                    long recipientId;
                    BigDecimal amount = amountGenerator.apply(threadNumber);
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        senderId = account2;
                        recipientId = account1;
                        amount = amount.negate();
                    } else {
                        senderId = account1;
                        recipientId = account2;
                    }
                    TransferStatus transferStatus = repository.makeTransfer(senderId, recipientId, amount);
                    counters.computeIfAbsent(transferStatus, status -> new AtomicInteger()).incrementAndGet();
                })
                .collect(Collectors.toList());
        runConcurrently(concurrentTransfers);
        return counters;
    }

    private Function<Integer, BigDecimal> deterministicBalanceGenerator() {
        return threadNumber -> {
            BigDecimal amount = amount(Math.sqrt(10 * (threadNumber + 1)), true);
            return threadNumber % 3 == 0 ? amount.negate() : amount;
        };
    }

    private Account account(long id, BigDecimal initialBalance) {
        Account account = new Account(initialBalance);
        accounts.put(id, account);
        return account;
    }
}