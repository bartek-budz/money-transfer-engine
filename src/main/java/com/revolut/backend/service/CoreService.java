package com.revolut.backend.service;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.domain.Transfer;
import com.revolut.backend.domain.TransferStatus;

import java.math.BigDecimal;
import java.util.List;


public class CoreService implements MoneyTransferService {

    private final AccountRepository accountRepository = new AccountRepository();
    private final TransferRepository transferRepository = new TransferRepository(accountRepository);

    @Override
    public long createAccount(BigDecimal initialBalance) {
        return accountRepository.createAccount(initialBalance);
    }

    @Override
    public BigDecimal checkBalance(long accountId) {
        return accountRepository.checkBalance(accountId);
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        return transferRepository.makeTransfer(senderId, recipientId, amount);
    }

    @Override
    public List<Transfer> getStatement(long accountId) {
        return transferRepository.getStatement(accountId);
    }
}
