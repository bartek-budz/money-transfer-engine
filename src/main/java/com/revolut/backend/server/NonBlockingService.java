package com.revolut.backend.server;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.api.TransferStatus;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class NonBlockingService {
    private final Executor writesExecutor = Executors.newSingleThreadExecutor();
    private final MoneyTransferService targetService;

    NonBlockingService(MoneyTransferService targetService) {
        this.targetService = targetService;
    }

    CompletionStage<Long> createAccount(BigDecimal initialBalance) {
        CompletableFuture<Long> futureId = new CompletableFuture<>();
        writesExecutor.execute(() -> futureId.complete(targetService.createAccount(initialBalance)));
        return futureId;
    }

    BigDecimal checkBalance(long id) {
        return targetService.checkBalance(id);
    }

    CompletionStage<TransferStatus> makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        CompletableFuture<TransferStatus> futureStatus = new CompletableFuture<>();
        writesExecutor.execute(() -> futureStatus.complete(targetService.makeTransfer(senderId, recipientId, amount)));
        return futureStatus;
    }
}
