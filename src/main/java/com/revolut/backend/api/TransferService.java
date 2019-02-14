package com.revolut.backend.api;

import com.revolut.backend.domain.Transfer;
import com.revolut.backend.domain.TransferStatus;

import java.math.BigDecimal;
import java.util.List;

public interface TransferService {
    TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount);

    List<Transfer> getStatement(long accountId);
}
