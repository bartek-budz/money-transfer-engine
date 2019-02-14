package com.revolut.backend.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.revolut.backend.utils.TestUtils.amount;
import static org.junit.jupiter.api.Assertions.*;

class TransferTest {

    @Test
    void outgoingShouldAssumeNegativeBalance() {
        //given
        final Instant timestamp = Instant.now();
        final long party = 1;
        final BigDecimal amount = amount(123.45);
        //when
        Transfer transfer = Transfer.outgoing(Instant.now(), party, amount);
        //then
        Assertions.assertEquals(timestamp, transfer.getTimestamp());
        Assertions.assertEquals(party, transfer.getParty());
        assertEquals(amount.negate(), transfer.getBalance());
    }

    @Test
    void incomingShouldAssumePositiveBalance() {
        //given
        final Instant timestamp = Instant.now();
        final long party = 0;
        final BigDecimal amount = amount(99.99);
        //when
        Transfer transfer = Transfer.incoming(Instant.now(), party, amount);
        //then
        Assertions.assertEquals(timestamp, transfer.getTimestamp());
        Assertions.assertEquals(party, transfer.getParty());
        assertEquals(amount, transfer.getBalance());
    }
}