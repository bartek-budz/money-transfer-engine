package com.revolut.backend.utils;

import com.revolut.backend.persistence.inmemory.InMemoryPersistenceProvider;
import com.revolut.backend.server.WebServer;

public class TestWebServer extends WebServer {

    private static final TestDatabase DATABASE = new TestDatabase();
    private static final TestWebServer SERVER = new TestWebServer(DATABASE);

    private TestWebServer(InMemoryPersistenceProvider persistenceProvider) {
        super(persistenceProvider);
    }

    public static TestWebServer webServer() {
        return SERVER;
    }

    public void eraseDatabase() {
        DATABASE.eraseDatabase();
    }

    public void restartServerAndDatabase() throws Exception {
        stop();
        DATABASE.resetDatabase();
        start();
    }
}
