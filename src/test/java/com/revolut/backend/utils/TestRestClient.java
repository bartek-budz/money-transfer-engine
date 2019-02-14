package com.revolut.backend.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.domain.Transfer;
import com.revolut.backend.domain.TransferStatus;
import com.revolut.backend.server.dto.CreateAccount;
import com.revolut.backend.server.dto.MakeTransfer;
import com.revolut.backend.server.dto.StatementEntry;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.groovy.io.StringBuilderWriter;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.revolut.backend.ConfigurationProperties.configuration;
import static io.restassured.RestAssured.given;

public class TestRestClient implements MoneyTransferService {

    private static final TestRestClient INSTANCE = new TestRestClient();

    private final ObjectMapper objectMapper = setUpObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory(objectMapper);

    private ObjectMapper setUpObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }

    private TestRestClient() {
    }

    public static TestRestClient restClient() {
        return INSTANCE;
    }

    @Override
    public long createAccount(BigDecimal initialBalance) {
        CreateAccount request = new CreateAccount(initialBalance);
        String resultString = postAndGetResponse(writeJson(request), "services/account/create");
        return Long.parseLong(resultString);
    }

    @Override
    public BigDecimal checkBalance(long accountId) {
        return new BigDecimal(get("services/account/balance/" + accountId));
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        MakeTransfer request = new MakeTransfer(senderId, recipientId, amount);
        String resultString = postAndGetResponse(writeJson(request), "services/transfer/make");
        return TransferStatus.ofCode(Integer.parseInt(resultString));
    }

    @Override
    public List<Transfer> getStatement(long accountId) {
        String response = get("services/transfer/statement/" + accountId);
        Collection<StatementEntry> outgoingTransfer = readJsonArray(response, StatementEntry.class);
        return outgoingTransfer.stream()
                .map(this::toTransfer)
                .collect(Collectors.toList());
    }

    private Transfer toTransfer(StatementEntry statementEntry) {
        return Transfer.builder()
                .timestamp(Instant.ofEpochMilli(statementEntry.getTimestamp()))
                .party(statementEntry.getParty())
                .balance(statementEntry.getBalance())
                .build();
    }

    private <RQ, RS> RS postJsonExpectJson(RQ request, Class<RS> responseClass, String path) {
        String requestJson = writeJson(request);
        String responseJson = postAndGetResponse(requestJson, path);
        return readJson(responseJson, responseClass);
    }

    private static String postAndGetResponse(String json, String path) {
        RequestSpecification request = given()
                .baseUri(configuration().getWebServerPublicAddress().toString())
                .port(configuration().getWebServerPort())
                .contentType(ContentType.JSON)
                .body(json);
        Response response = request.post(path);
        return response.getBody().print();
    }

    private static String get(String path) {
        RequestSpecification request = given()
                .baseUri(configuration().getWebServerPublicAddress().toString())
                .port(configuration().getWebServerPort());
        Response response = request.get(path);
        return response.getBody().print();
    }

    private String writeJson(Object object) {
        try {
            Writer stringWriter = new StringBuilderWriter();
            jsonFactory.createGenerator(stringWriter).writeObject(object);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T> T readJson(String json, Class<T> clazz) {
        try {
            return jsonFactory.createParser(json).readValueAs(clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T> Collection<T> readJsonArray(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
