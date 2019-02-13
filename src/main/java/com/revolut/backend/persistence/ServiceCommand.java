package com.revolut.backend.persistence;

import com.revolut.backend.api.MoneyTransferService;

import java.io.Serializable;
import java.util.function.Function;

public interface ServiceCommand<R> extends Function<MoneyTransferService, R>, Serializable {
}
