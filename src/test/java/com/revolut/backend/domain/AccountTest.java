package com.revolut.backend.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.revolut.backend.utils.TestUtils.amount;
import static com.revolut.backend.utils.TestUtils.assertEquals;

class AccountTest {

    @Test
    void instanceShouldBeCreatedWithInitialBalance() {
        final BigDecimal initialBalance = amount(12.34);
        //given
        Account account = new Account(initialBalance);
        //when
        BigDecimal balance = account.getBalance();
        //then
        assertEquals(initialBalance, balance);
    }

    @Test
    void depositShouldAddGivenAmountToBalance() {
        //given
        Account account = new Account(amount(178.5));
        //when
        account.deposit(amount(1330.01));
        BigDecimal finalBalance = account.getBalance();
        //then
        assertEquals(amount(1508.51), finalBalance);
    }

    @Test
    void withdrawShouldSubtractGivenAmountFromBalance() {
        //given
        Account account = new Account(amount(4500.43));
        //when
        account.withdraw(amount(499.99));
        BigDecimal finalBalance = account.getBalance();
        //then
        assertEquals(amount(4000.44), finalBalance);
    }
}