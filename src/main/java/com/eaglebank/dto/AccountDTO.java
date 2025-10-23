package com.eaglebank.dto;

import com.eaglebank.model.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AccountDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sortCode;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Account type is required and must be either 'personal' or 'business'")
    private AccountType accountType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long balance;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String currency;

    public AccountDTO() {
    }

    public AccountDTO(String accountNumber, String sortCode, String name, AccountType accountType, long balance, String currency) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public long getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}

