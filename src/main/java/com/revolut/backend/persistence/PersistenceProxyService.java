package com.revolut.backend.persistence;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.api.TransferStatus;

import java.math.BigDecimal;

public class PersistenceProxyService implements MoneyTransferService {

    private final PersistenceProvider persistenceProvider;

    public PersistenceProxyService(PersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    @Override
    public long createAccount(BigDecimal initialBalance) {
        return persistenceProvider.executeAndQuery(service -> service.createAccount(initialBalance));
    }

    @Override
    public BigDecimal checkBalance(long id) {
        return persistenceProvider.query(service -> service.checkBalance(id));
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        return persistenceProvider.executeAndQuery(service -> service.makeTransfer(senderId, recipientId, amount));
    }
}
