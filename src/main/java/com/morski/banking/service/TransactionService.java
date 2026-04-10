package com.morski.banking.service;

import com.morski.banking.entity.Transaction;
import com.morski.banking.entity.TransactionType;
import com.morski.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void recordTransaction(UUID accountId, java.math.BigDecimal amount, TransactionType type, UUID relatedAccountId, String description) {
        Transaction tx = new Transaction(accountId, amount, type, relatedAccountId, description);
        transactionRepository.save(tx);
    }

    public Page<Transaction> getTransactions(UUID accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId, pageable);
    }
}
