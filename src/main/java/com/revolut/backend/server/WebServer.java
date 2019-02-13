package com.revolut.backend.server;

import com.revolut.backend.api.TransferStatus;
import com.revolut.backend.persistence.PersistenceProvider;
import com.revolut.backend.server.dto.CreateAccount;
import com.revolut.backend.server.dto.MakeTransfer;
import com.revolut.backend.persistence.inmemory.InMemoryPersistenceProvider;
import com.revolut.backend.persistence.PersistenceProxyService;
import ratpack.error.ServerErrorHandler;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.jackson.Jackson;
import ratpack.jackson.JsonRender;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;


public class WebServer {

    private final NonBlockingService service;
    private RatpackServer server;

    private WebServer() {
        this(new InMemoryPersistenceProvider());
    }

    public WebServer(PersistenceProvider persistenceProvider) {
        this.service = new NonBlockingService(new PersistenceProxyService(persistenceProvider));
    }

    public void start() throws Exception {
        this.server = RatpackServer.start(server -> server
                .serverConfig(
                        ServerConfig
                                .embedded()
                                .publicAddress(new URI("http://localhost"))
                                .port(8085)
                                .threads(4)
                )
                .handlers(root -> {
                            root.prefix("services", services -> services
                                    .prefix("account", account -> account
                                            .prefix("create", createAccountAction())
                                            .prefix("balance", createCheckBalanceAction())
                                    )
                                    .prefix("transfer", account -> account
                                            .prefix("make", createMakeTransferAction())
                                    )
                            ).register(registry ->
                                    registry.add(ServerErrorHandler.class, (context, throwable) ->
                                            context.render("Caught by error handler: " + throwable.getMessage())
                                    ));

                        }
                )
        );
    }

    private Action<Chain> createAccountAction() {
        return createRequestResponseAction(CreateAccount.class, request -> service.createAccount(request.getInitialBalance()), Function.identity());
    }

    private Action<Chain> createCheckBalanceAction() {
        return orderChain -> orderChain
                .path(":id", ctx -> {
                    Map<String, String> pathTokens = ctx.getPathTokens();
                    String id = pathTokens.get("id");
                    BigDecimal balance = service.checkBalance(Long.parseLong(id));
                    ctx.render(balance.toString());
                });
    }

    private Action<Chain> createMakeTransferAction() {
        return createRequestResponseAction(MakeTransfer.class, request -> service.makeTransfer(request.getSenderId(), request.getRecipientId(), request.getAmount()), TransferStatus::getCode);
    }


    private <RQ, T, RS> Action<Chain> createRequestResponseAction(Class<RQ> requestType, Function<RQ, CompletionStage<T>> processor, Function<T, RS> responseCreator) {
        return orderChain -> orderChain
                .post(context -> {
                    context.parse(requestType)
                            .onError(System.out::println)
                            .then(request -> {
                                CompletionStage<JsonRender> response = processor.apply(request)
                                        .thenApply(responseCreator)
                                        .thenApply(Jackson::json);

                                Promise promise = Promise.async(downstream -> downstream.accept(response));
                                context.render(promise);
                            });
                });
    }

    public static void main(String... args) throws Exception {
        new WebServer().start();
    }


    public void stop() throws Exception {
        server.stop();
    }
}
