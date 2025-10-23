package com.eaglebank.contoller;

import com.eaglebank.dto.AccountDTO;
import com.eaglebank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@RequestHeader("user-id") String userId, @Valid @RequestBody AccountDTO accountDTO) {
        return accountService.createAccount(userId, accountDTO);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getAccountById(@PathVariable("accountId") String accountId) {
        return accountService.getAccountById(accountId);
    }
}
