package com.eaglebank.service;

import com.eaglebank.dto.AccountDTO;
import com.eaglebank.exception.AccountNotFoundException;
import com.eaglebank.model.Account;
import com.eaglebank.model.AccountType;
import com.eaglebank.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(1234567L);
    private static final AtomicLong SORT_COUNTER = new AtomicLong(101010L);

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^usr-[A-Za-z0-9]{5}$");

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDTO createAccount(String userId, AccountDTO dto) {
        if (userId == null || !USER_ID_PATTERN.matcher(userId).matches()) {
            throw new IllegalArgumentException("user-id header is required and must match pattern 'usr-xxxxx'");
        }

        if (accountRepository.existsById(userId)) {
            throw new IllegalStateException("An account with the provided user-id already exists");
        }

        AccountType type = dto.getAccountType();
        if (type == null) {
            throw new IllegalArgumentException("accountType is required and must be one of 'personal' or 'business'");
        }

        String accountNumber;
        String sortCode;

        int attempts = 0;
        final int maxAttempts = 10;
        do {
            accountNumber = generateAccountNumber();
            attempts++;
        } while (accountRepository.existsByAccountNumber(accountNumber) && attempts < maxAttempts);

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalStateException("Failed to generate unique account number");
        }

        attempts = 0;
        do {
            sortCode = generateSortCode();
            attempts++;
        } while (accountRepository.existsBySortCode(sortCode) && attempts < maxAttempts);

        if (accountRepository.existsBySortCode(sortCode)) {
            throw new IllegalStateException("Failed to generate unique sort code");
        }

        Account account = new Account();
        account.setId(userId);
        account.setAccountNumber(accountNumber);
        account.setSortCode(sortCode);
        account.setName(dto.getName());
        account.setAccountType(type);
        account.setBalance(0L);
        account.setCurrency("GBP");

        Account saved = accountRepository.save(account);
        return new AccountDTO(saved.getAccountNumber(), saved.getSortCode(), saved.getName(), saved.getAccountType(), saved.getBalance(), saved.getCurrency());
    }

    public AccountDTO getAccountById(String id) {
        return accountRepository.findById(id)
                .map(a -> new AccountDTO(a.getAccountNumber(), a.getSortCode(), a.getName(), a.getAccountType(), a.getBalance(), a.getCurrency()))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private String generateAccountNumber() {
        long next = ACCOUNT_COUNTER.getAndIncrement();
        return String.format("%08d", next);
    }

    private String generateSortCode() {
        long next = SORT_COUNTER.getAndIncrement();
        String raw = String.format("%06d", next);
        return raw.substring(0,2) + "-" + raw.substring(2,4) + "-" + raw.substring(4,6);
    }
}
