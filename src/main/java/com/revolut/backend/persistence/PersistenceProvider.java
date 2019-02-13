package com.revolut.backend.persistence;

public interface PersistenceProvider {

    <R> R executeAndQuery(ServiceCommand<R> command);

    <R> R query(ServiceCommand<R> query);
}
