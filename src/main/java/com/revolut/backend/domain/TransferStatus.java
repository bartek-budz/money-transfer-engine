package com.revolut.backend.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum TransferStatus {
    TRANSFERRED(0),
    INTERNAL_ERROR(1),
    INVALID_SENDER(2),
    INVALID_RECIPIENT(3),
    INVALID_AMOUNT(4),
    NO_FUNDS(5);

    private final int code;

    TransferStatus(int code) {
        this.code = code;
    }

    private static final Map<Integer, TransferStatus> CODE_TO_VALUE;

    static {
        Map<Integer, TransferStatus> map = new ConcurrentHashMap<>();
        Arrays.stream(TransferStatus.values()).forEach(value -> map.put(value.code, value));
        CODE_TO_VALUE = Collections.unmodifiableMap(map);
    }

    public static TransferStatus ofCode(int code) {
        return CODE_TO_VALUE.get(code);
    }

    public int getCode() {
        return code;
    }
}
