package com.revolut.backend.utils;

import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.service.CoreService;
import com.revolut.backend.persistence.inmemory.InMemoryPersistenceProvider;
import pl.setblack.airomem.core.PersistenceController;
import pl.setblack.airomem.core.builders.PrevaylerBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TestDatabase extends InMemoryPersistenceProvider {

    private Path lastUsedDbFolder;

    @Override
    protected PersistenceController<MoneyTransferService> createNewPersistenceController() {
        return createPersistenceController(getNewTempDbFolder());
    }

    void resetDatabase() {
        persistenceController = createPersistenceController(lastUsedDbFolder);
    }

    void eraseDatabase() {
        persistenceController = createPersistenceController(getNewTempDbFolder());
    }

    private Path getNewTempDbFolder() {
        try {
            return Files.createTempDirectory("test_mte_");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PersistenceController<MoneyTransferService> createPersistenceController(Path dbFolder) {
        assert dbFolder != null;
        PersistenceController<MoneyTransferService> controller = PrevaylerBuilder.<MoneyTransferService>newBuilder()
                .withFolder(dbFolder)
                .useSupplier(CoreService::new)
                .disableRoyalFoodTester()
                .build();
        this.lastUsedDbFolder = dbFolder;
        return controller;
    }
}
