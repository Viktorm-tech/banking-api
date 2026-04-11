package com.morski.banking.service;

import com.morski.banking.entity.Account;
import com.morski.banking.entity.TransactionType;
import com.morski.banking.exception.AccountNotFoundException;
import com.morski.banking.exception.InsufficientBalanceException;
import com.morski.banking.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final LimitsChecker limitsChecker;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionService transactionService,
                          LimitsChecker limitsChecker, KafkaEventPublisher kafkaEventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.limitsChecker = limitsChecker;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Transactional
    public Account createAccount(String customerId, BigDecimal initialBalance, String currency) {
        Account account = new Account(customerId, initialBalance, currency);
        Account saved = accountRepository.save(account);
        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            transactionService.recordTransaction(saved.getId(), initialBalance, TransactionType.DEPOSIT, null, "Initial deposit");
        }
        kafkaEventPublisher.publishAccountCreated(saved.getId(), Map.of("customerId", customerId, "initialBalance", initialBalance, "currency", currency));
        return saved;
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
    }

    @Transactional
    public void deposit(UUID id, BigDecimal amount) {
        Account account = getAccount(id);
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        transactionService.recordTransaction(id, amount, TransactionType.DEPOSIT, null, "Deposit");
        log.info("Deposited {} to account {}", amount, id);
    }

    @Transactional
    public void withdraw(UUID id, BigDecimal amount) {
        Account account = getAccount(id);
        limitsChecker.checkLimit(amount);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        transactionService.recordTransaction(id, amount, TransactionType.WITHDRAW, null, "Withdrawal");
        log.info("Withdrew {} from account {}", amount, id);
    }

    @Transactional
    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        Account fromAccount = getAccount(fromId);
        Account toAccount = getAccount(toId);
        limitsChecker.checkLimit(amount);
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        fromAccount.setUpdatedAt(LocalDateTime.now());
        toAccount.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionService.recordTransaction(fromId, amount, TransactionType.TRANSFER_SENT, toId, "Transfer to " + toId);
        transactionService.recordTransaction(toId, amount, TransactionType.TRANSFER_RECEIVED, fromId, "Transfer from " + fromId);
        kafkaEventPublisher.publishTransferCompleted(fromId, toId, Map.of("amount", amount, "toAccount", toId));
        log.info("Transferred {} from {} to {}", amount, fromId, toId);
    }
}