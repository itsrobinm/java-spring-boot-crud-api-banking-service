package com.eaglebank.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    PERSONAL("personal"),
    BUSINESS("business");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountType fromValue(String v) {
        if (v == null) return null;
        String norm = v.trim().toLowerCase();
        switch (norm) {
            case "personal":
                return PERSONAL;
            case "business":
                return BUSINESS;
            default:
                throw new IllegalArgumentException("Invalid accountType: " + v);
        }
    }
}

