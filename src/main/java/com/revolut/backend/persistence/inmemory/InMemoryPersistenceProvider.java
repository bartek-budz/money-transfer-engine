package com.revolut.backend.persistence.inmemory;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.persistence.PersistenceProvider;
import com.revolut.backend.persistence.ServiceCommand;
import com.revolut.backend.service.CoreService;
import pl.setblack.airomem.core.PersistenceController;
import pl.setblack.airomem.core.builders.PrevaylerBuilder;

public class InMemoryPersistenceProvider implements PersistenceProvider {

    protected PersistenceController<MoneyTransferService> persistenceController;

    public InMemoryPersistenceProvider() {
        this.persistenceController = createNewPersistenceController();
    }

    protected PersistenceController<MoneyTransferService> createNewPersistenceController() {
        return PrevaylerBuilder.<MoneyTransferService>newBuilder()
                .withinUserFolder("money-transfer-engine")
                .useSupplier(CoreService::new)
                .disableRoyalFoodTester()
                .build();
    }

    @Override
    public <R> R executeAndQuery(ServiceCommand<R> command) {
        return persistenceController.executeAndQuery(command::apply);
    }

    @Override
    public <R> R query(ServiceCommand<R> query) {
        return persistenceController.query(query::apply);
    }
}
