package com.revolut.backend.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.backend.api.MoneyTransferService;
import com.revolut.backend.api.TransferStatus;
import com.revolut.backend.server.dto.CreateAccount;
import com.revolut.backend.server.dto.MakeTransfer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.groovy.io.StringBuilderWriter;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

public class TestRestClient implements MoneyTransferService {

    private static final int SERVER_PORT = 8085;

    private static final TestRestClient INSTANCE = new TestRestClient();

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

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
    public BigDecimal checkBalance(long id) {
        return new BigDecimal(get("services/account/balance/" + id));
    }

    @Override
    public TransferStatus makeTransfer(long senderId, long recipientId, BigDecimal amount) {
        MakeTransfer request = new MakeTransfer(senderId, recipientId, amount);
        String resultString = postAndGetResponse(writeJson(request), "services/transfer/make");
        return TransferStatus.ofCode(Integer.parseInt(resultString));
    }

    private <RQ, RS> RS postJsonExpectJson(RQ request, Class<RS> responseClass, String path) {
        String requestJson = writeJson(request);
        String responseJson = postAndGetResponse(requestJson, path);
        return readJson(responseJson, responseClass);
    }

    public static String postAndGetResponse(String json, String path) {
        RequestSpecification request = given()
                .port(SERVER_PORT)
                .contentType(ContentType.JSON)
                .body(json);
        Response response = request.post(path);
        return response.getBody().print();
    }

    public static String get(String path) {
        RequestSpecification request = given()
                .port(SERVER_PORT);
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
}
