package com.revolut.backend.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class Transfer implements Serializable {
    private Instant timestamp;
    private long party;
    private BigDecimal balance;

    public static Transfer outgoing(Instant timestamp, long recipientId, BigDecimal amount) {
        return Transfer.builder().timestamp(timestamp).party(recipientId).balance(amount.negate()).build();
    }

    public static Transfer incoming(Instant timestamp, long senderId, BigDecimal amount) {
        return Transfer.builder().timestamp(timestamp).party(senderId).balance(amount).build();
    }
}
