package com.revolut.backend.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Account implements Serializable {

    private AtomicReference<BigDecimal> balance = new AtomicReference<>();

    public Account(BigDecimal initialBalance) {
        this.balance.set(initialBalance);
    }

    public BigDecimal getBalance() {
        return balance.get();
    }

    public void deposit(BigDecimal amount) {
        balance.accumulateAndGet(amount, BigDecimal::add);
    }

    public void withdraw(BigDecimal amount) {
        balance.accumulateAndGet(amount, BigDecimal::subtract);
    }
}
