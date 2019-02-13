package com.revolut.backend.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateAccount {
    final BigDecimal initialBalance;

    public CreateAccount(@JsonProperty("initialBalance") BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
