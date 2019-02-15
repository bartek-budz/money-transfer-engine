package com.revolut.backend.service;

import com.revolut.backend.api.TransferService;
import com.revolut.backend.domain.Account;
import com.revolut.backend.domain.Transfer;
import com.revolut.backend.domain.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.revolut.backend.domain.Transfer.incoming;
import static com.revolut.backend.domain.Transfer.outgoing;
import static com.revolut.backend.domain.TransferStatus.*;

public class TransferRepository implements TransferService, Serializable {

    private final AccountLocator accountLocator;
    private final ConcurrentHashMap<Long, Semaphore> mutexByAccountId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Transfer>> transfersById = new ConcurrentHashMap<>();

    TransferRepository(AccountLocator accountLocator) {
        this.accountLocator = accountLocator;
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        try {
            validateTransfer(senderId, recipientId, amount);
            makeTransfer(getAccount(senderId, INVALID_SENDER), getAccount(recipientId, INVALID_RECIPIENT), amount, senderId);
            recordTransfer(senderId, recipientId, amount);
            return TransferStatus.TRANSFERRED;
        } catch (TransferFailedException e) {
            return e.getFailureReason();
        }
    }

    private void validateTransfer(long senderId, long recipientId, BigDecimal amount) throws TransferFailedException {
        if (senderId == recipientId) {
            throw createTransferFailedException(INVALID_RECIPIENT);
        }
        if (amount == null || BigDecimal.ZERO.compareTo(amount) >= 0) {
            throw createTransferFailedException(INVALID_AMOUNT);
        }
    }

    private void makeTransfer(Account sender, Account recipient, BigDecimal amount, long lockId) throws TransferFailedException {
        try {
            acquireLock(lockId);
            if (sender.getBalance().compareTo(amount) < 0) {
                throw createTransferFailedException(NO_FUNDS);
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
                throw createTransferFailedException(INTERNAL_ERROR);
            }
        } catch (InterruptedException e) {
            throw createTransferFailedException(INTERNAL_ERROR);
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

    private void recordTransfer(long senderId, long recipientId, BigDecimal amount) {
        Instant timestamp = Instant.now();
        statement(senderId).add(outgoing(timestamp, recipientId, amount));
        statement(recipientId).add(incoming(timestamp, senderId, amount));
    }

    private ConcurrentLinkedQueue<Transfer> statement(long accountId) {
        return transfersById.computeIfAbsent(accountId, id -> new ConcurrentLinkedQueue<>());
    }

    @Override
    public List<Transfer> getStatement(long accountId) {
        accountLocator.validateAccountId(accountId);
        return new ArrayList<>(statement(accountId));
    }
}
