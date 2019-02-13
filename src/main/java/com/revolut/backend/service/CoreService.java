package com.revolut.backend.service;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.api.TransferStatus;

import java.math.BigDecimal;


public class CoreService implements MoneyTransferService {

    private final AccountRepository accountRepository = new AccountRepository();
    private final TransferRepository transferRepository = new TransferRepository(accountRepository);

    @Override
    public long createAccount(BigDecimal initialBalance) {
        return accountRepository.createAccount(initialBalance);
    }

    @Override
    public BigDecimal checkBalance(long id) {
        return accountRepository.checkBalance(id);
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        return transferRepository.makeTransfer(senderId, recipientId, amount);
    }
}
