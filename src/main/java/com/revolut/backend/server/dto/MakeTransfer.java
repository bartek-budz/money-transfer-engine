package com.revolut.backend.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MakeTransfer {
    private final long senderId;
    private final long recipientId;
    private BigDecimal amount;

    public MakeTransfer(@JsonProperty("senderId") long senderId,
                        @JsonProperty("recipientId") long recipientId,
                        @JsonProperty("amount") BigDecimal amount) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
    }
}
