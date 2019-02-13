package com.revolut.backend.api;

import com.revolut.backend.utils.TestUtils;
import com.revolut.backend.utils.TestServerRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.revolut.backend.utils.TestRestClient.restClient;
import static com.revolut.backend.utils.TestUtils.amount;
import static com.revolut.backend.utils.TestUtils.assertEquals;
import static com.revolut.backend.utils.TestWebServer.webServer;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestServerRunner.class)
class RestServiceTest {

    @Test
    void shouldReturnUniqueAccountId() {
        final int howMany = 7;
        Set<Long> ids = IntStream.range(0, howMany).boxed()
                .map(i -> restClient().createAccount())
                .collect(Collectors.toSet());
        assertEquals(howMany, ids.size());
    }

    @Test
    void shouldSetInitialBalance() {
        //given
        final BigDecimal initialBalance = BigDecimal.valueOf(10567.345);
        long id = restClient().createAccount(initialBalance);
        //when
        BigDecimal actualBalance = restClient().checkBalance(id);
        //then
        Assertions.assertEquals(initialBalance, actualBalance);
    }

    @Test
    void shouldDiscoverSenderNotExists() {
        //given
        long senderId = 9999;
        long recipientId = restClient().createAccount();
        //when
        TransferStatus status = restClient().makeTransfer(senderId, recipientId, BigDecimal.ONE);
        //then
        assertEquals(TransferStatus.FAILED_SENDER_NOT_FOUND, status);
    }

    @Test
    void shouldDiscoverRecipientNotExists() {
        //given
        long senderId = restClient().createAccount();
        long recipientId = 9999;
        //when
        TransferStatus status = restClient().makeTransfer(senderId, recipientId, BigDecimal.ONE);
        //then
        assertEquals(TransferStatus.FAILED_RECIPIENT_NOT_FOUND, status);
    }

    @Test
    void shouldNotAllowForNullAmountTransfer() {
        assertEquals(TransferStatus.FAILED_INVALID_AMOUNT, makeTransferWithAmount(null));
    }

    @Test
    void shouldNotAllowForZeroAmountTransfer() {
        assertEquals(TransferStatus.FAILED_INVALID_AMOUNT, makeTransferWithAmount(BigDecimal.ZERO));
    }

    @Test
    void shouldNotAllowForNegativeAmountTransfer() {
        assertEquals(TransferStatus.FAILED_INVALID_AMOUNT, makeTransferWithAmount(amount(-0.1)));
    }

    private TransferStatus makeTransferWithAmount(BigDecimal amount) {
        long senderId = restClient().createAccount();
        long recipientId = restClient().createAccount();
        return restClient().makeTransfer(senderId, recipientId, amount);
    }

    @Test
    void shouldNotAllowForDebit() {
        //given
        long senderId = restClient().createAccount(amount(11.22));
        long recipientId = restClient().createAccount();
        //when
        TransferStatus status = restClient().makeTransfer(senderId, recipientId, amount(11.23));
        //then
        assertEquals(TransferStatus.DENIED_LACK_OF_FUNDS, status);
    }

    @Test
    void shouldUpdateBothAccountBalances() {
        final BigDecimal account1Balance = amount(20.03);
        final BigDecimal account2Balance = amount(3.42);
        final BigDecimal transferAmount = amount(12.34);
        //given
        long senderId = restClient().createAccount(account1Balance);
        long recipientId = restClient().createAccount(account2Balance);
        Assertions.assertEquals(account1Balance, restClient().checkBalance(senderId));
        Assertions.assertEquals(account2Balance, restClient().checkBalance(recipientId));
        //when
        TransferStatus status = restClient().makeTransfer(senderId, recipientId, transferAmount);
        //then
        assertEquals(TransferStatus.SUCCESS, status);
        assertEquals(account1Balance.subtract(transferAmount), restClient().checkBalance(senderId));
        assertEquals(account2Balance.add(transferAmount), restClient().checkBalance(recipientId));
    }

    @Test
    void balanceShouldReflectAllTransactions() {
        //given
        long accountId = restClient().createAccount(amount(100));
        long otherAccountId = restClient().createAccount(amount(50.50));
        long yetAnotherAccountId = restClient().createAccount(amount(9.99));
        //when
        restClient().makeTransfer(accountId, otherAccountId, amount(30.21));
        restClient().makeTransfer(yetAnotherAccountId, accountId, amount(5.40));
        restClient().makeTransfer(otherAccountId, yetAnotherAccountId, amount(30.00));
        //then
        assertEquals(amount(75.19), restClient().checkBalance(accountId));
        assertEquals(amount(50.71), restClient().checkBalance(otherAccountId));
        assertEquals(amount(34.59), restClient().checkBalance(yetAnotherAccountId));
    }

    @Test
    void balanceShouldReflectAllConcurrentTransactions() {
        //given
        final long millionaire = restClient().createAccount(amount(1000000));
        final long bankrupt = restClient().createAccount(amount(0));
        //when
        final double multiplier = 99.01;
        List<Runnable> concurrentOperations = IntStream.rangeClosed(0, 100).boxed()
                .map(threadNumber -> (Runnable) () -> restClient().makeTransfer(millionaire, bankrupt, amount(threadNumber * multiplier, true)))
                .collect(Collectors.toList());
        TestUtils.runConcurrently(concurrentOperations);
        //then
        assertEquals(amount(499999.5), restClient().checkBalance(millionaire));
        assertEquals(amount(500000.5), restClient().checkBalance(bankrupt));
    }

    @Test
    void dataShouldBeAvailableAfterServerRestart() throws Exception {
        final long account1Id = restClient().createAccount(amount(10000));
        final long account2Id = restClient().createAccount(amount(34523.67));

        webServer().restartServerAndDatabase();
        Assertions.assertEquals(amount(34523.67), restClient().checkBalance(account2Id));
        Assertions.assertEquals(amount(10000), restClient().checkBalance(account1Id));
        restClient().makeTransfer(account1Id, account2Id, amount(123.45));

        webServer().restartServerAndDatabase();
        Assertions.assertEquals(amount(9876.55), restClient().checkBalance(account1Id));
        Assertions.assertEquals(amount(34647.12), restClient().checkBalance(account2Id));
    }
}
