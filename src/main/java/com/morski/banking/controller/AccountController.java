package com.morski.banking.controller;

import com.morski.banking.dto.AccountResponse;
import com.morski.banking.dto.CreateAccountRequest;
import com.morski.banking.dto.DepositWithdrawRequest;
import com.morski.banking.dto.TransactionResponse;
import com.morski.banking.dto.TransferRequest;
import com.morski.banking.entity.Account;
import com.morski.banking.entity.Transaction;
import com.morski.banking.service.AccountService;
import com.morski.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.customerId(), request.initialBalance(), request.currency());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(toResponse(account));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID id, @Valid @RequestBody DepositWithdrawRequest request) {
        accountService.deposit(id, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable UUID id, @Valid @RequestBody DepositWithdrawRequest request) {
        accountService.withdraw(id, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<Void> transfer(@PathVariable UUID id, @Valid @RequestBody TransferRequest request) {
        accountService.transfer(id, request.toAccountId(), request.amount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@PathVariable UUID id,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        Page<Transaction> transactionPage = transactionService.getTransactions(id, PageRequest.of(page, size));
        Page<TransactionResponse> responsePage = transactionPage.map(this::toTransactionResponse);
        return ResponseEntity.ok(responsePage);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getCustomerId(), account.getBalance(), account.getCurrency(), account.getStatus());
    }

    private TransactionResponse toTransactionResponse(Transaction tx) {
        return new TransactionResponse(tx.getId(), tx.getAmount(), tx.getType(), tx.getRelatedAccountId(), tx.getDescription(), tx.getTimestamp());
    }
}
