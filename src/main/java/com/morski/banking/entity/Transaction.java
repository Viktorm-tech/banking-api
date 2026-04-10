package com.morski.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private UUID relatedAccountId;

    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // constructors, getters, setters
    public Transaction() {}

    public Transaction(UUID accountId, BigDecimal amount, TransactionType type, UUID relatedAccountId, String description) {
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.relatedAccountId = relatedAccountId;
        this.description = description;
    }

    // getters and setters omitted for brevity, but should be present
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public UUID getRelatedAccountId() { return relatedAccountId; }
    public void setRelatedAccountId(UUID relatedAccountId) { this.relatedAccountId = relatedAccountId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
