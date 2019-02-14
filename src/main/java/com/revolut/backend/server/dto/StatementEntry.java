package com.revolut.backend.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.revolut.backend.domain.Transfer;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StatementEntry {
    private long timestamp;
    private long party;
    private BigDecimal balance;

    public StatementEntry(@JsonProperty("timestamp") long timestamp,
                          @JsonProperty("party") long party,
                          @JsonProperty("balance") BigDecimal balance) {
        this.timestamp = timestamp;
        this.party = party;
        this.balance = balance;
    }

    public static StatementEntry fromTransfer(Transfer transfer) {
        return new StatementEntry(transfer.getTimestamp().toEpochMilli(), transfer.getParty(), transfer.getBalance());
    }
}
