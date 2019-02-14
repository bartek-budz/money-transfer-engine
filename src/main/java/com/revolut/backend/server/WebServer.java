package com.revolut.backend.server;

import com.revolut.backend.domain.TransferStatus;
import com.revolut.backend.persistence.PersistenceProvider;
import com.revolut.backend.persistence.PersistenceProxyService;
import com.revolut.backend.persistence.inmemory.InMemoryPersistenceProvider;
import com.revolut.backend.server.dto.CreateAccount;
import com.revolut.backend.server.dto.MakeTransfer;
import com.revolut.backend.server.dto.StatementEntry;
import ratpack.error.ServerErrorHandler;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.jackson.Jackson;
import ratpack.jackson.JsonRender;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.revolut.backend.ConfigurationProperties.configuration;
import static ratpack.jackson.Jackson.json;


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
                                .publicAddress(configuration().getWebServerPublicAddress())
                                .port(configuration().getWebServerPort())
                                .threads(configuration().getWebServerThreads())
                )
                .handlers(root -> {
                            root.prefix("services", services -> services
                                    .prefix("account", account -> account
                                            .prefix("create", createAccountAction())
                                            .prefix("balance", createCheckBalanceAction())
                                    )
                                    .prefix("transfer", account -> account
                                            .prefix("make", createMakeTransferAction())
                                            .prefix("statement", createGetStatementAction())
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

    private Action<Chain> createGetStatementAction() {
        return orderChain -> orderChain
                .path(":id", ctx -> {
                    Map<String, String> pathTokens = ctx.getPathTokens();
                    long accountId = Long.parseLong(pathTokens.get("id"));
                    List<StatementEntry> statement = service.getStatement(accountId).stream()
                            .map(StatementEntry::fromTransfer)
                            .collect(Collectors.toList());
                    ctx.render(json(statement));
                });
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
