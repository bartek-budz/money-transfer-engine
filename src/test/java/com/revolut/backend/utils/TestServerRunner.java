package com.revolut.backend.utils;

import org.junit.jupiter.api.extension.*;

import static com.revolut.backend.utils.TestWebServer.webServer;

public class TestServerRunner implements BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        webServer().start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        webServer().eraseDatabase();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        webServer().stop();
    }
}
