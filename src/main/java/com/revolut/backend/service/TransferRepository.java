package com.revolut.backend.service;

import com.revolut.backend.api.TransferService;
import com.revolut.backend.api.TransferStatus;
import com.revolut.backend.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.revolut.backend.api.TransferStatus.*;

public class TransferRepository implements TransferService, Serializable {

    private final AccountLocator accountLocator;
    private final ConcurrentHashMap<Long, Semaphore> mutexByAccountId = new ConcurrentHashMap<>();

    TransferRepository(AccountLocator accountLocator) {
        this.accountLocator = accountLocator;
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        try {
            validateAmount(amount);
            makeTransfer(getAccount(senderId, FAILED_SENDER_NOT_FOUND), getAccount(recipientId, FAILED_RECIPIENT_NOT_FOUND), amount, senderId);
            return TransferStatus.SUCCESS;
        } catch (TransferFailedException e) {
            return e.getFailureReason();
        }
    }

    private void validateAmount(BigDecimal amount) throws TransferFailedException {
        if (amount == null || BigDecimal.ZERO.compareTo(amount) >= 0) {
            throw new TransferFailedException(FAILED_INVALID_AMOUNT);
        }
    }

    private void makeTransfer(Account sender, Account recipient, BigDecimal amount, long lockId) throws TransferFailedException {
        try {
            acquireLock(lockId);
            if (sender.getBalance().compareTo(amount) < 0) {
                throw createTransferFailedException(DENIED_LACK_OF_FUNDS);
            }
            sender.withdraw(amount);
            recipient.deposit(amount);
        } finally {
            releaseLock(lockId);
        }
    }

    private Account getAccount(long senderId, TransferStatus statusIfNotFound) throws TransferFailedException {
        return accountLocator.getAccount(senderId).orElseThrow(() -> createTransferFailedException(statusIfNotFound));
    }

    private void acquireLock(long accountId) throws TransferFailedException {
        try {
            Semaphore semaphore = mutexByAccountId.computeIfAbsent(accountId, id -> new Semaphore(1));
            if (!semaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                throw createTransferFailedException(FAILED_INTERNAL_ERROR);
            }
        } catch (InterruptedException e) {
            throw createTransferFailedException(FAILED_INTERNAL_ERROR);
        }
    }

    private void releaseLock(long accountId) {
        if (mutexByAccountId.containsKey(accountId)) {
            mutexByAccountId.get(accountId).release();
        }
    }

    private TransferFailedException createTransferFailedException(TransferStatus reason) {
        return new TransferFailedException(reason);
    }

    @AllArgsConstructor
    @Getter
    private class TransferFailedException extends Exception {
        private TransferStatus failureReason;
    }
}
