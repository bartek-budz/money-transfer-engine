package com.revolut.backend.persistence;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.domain.Transfer;
import com.revolut.backend.domain.TransferStatus;

import java.math.BigDecimal;
import java.util.List;

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
    public BigDecimal checkBalance(long accountId) {
        return persistenceProvider.query(service -> service.checkBalance(accountId));
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        return persistenceProvider.executeAndQuery(service -> service.makeTransfer(senderId, recipientId, amount));
    }

    @Override
    public List<Transfer> getStatement(long accountId) {
        return persistenceProvider.query(service -> service.getStatement(accountId));
    }
}
