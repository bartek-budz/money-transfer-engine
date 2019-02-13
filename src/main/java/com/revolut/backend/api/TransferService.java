package com.revolut.backend.api;

import java.math.BigDecimal;

public interface TransferService {
    TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount);

}
