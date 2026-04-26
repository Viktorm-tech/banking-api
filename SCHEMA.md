# Database Schema

## Table `accounts`

Stores bank account information.

| Column         | Type                     | Constraints                | Description                              |
|----------------|--------------------------|----------------------------|------------------------------------------|
| `id`           | `UUID`                   | PRIMARY KEY                | Unique account identifier                |
| `customer_id`  | `VARCHAR(255)`           | NOT NULL                   | External customer identifier             |
| `balance`      | `DECIMAL(19,2)`          | NOT NULL                   | Current account balance                  |
| `currency`     | `CHAR(3)`                | NOT NULL                   | Currency code (USD, EUR, RUB, etc.)      |
| `status`       | `VARCHAR(20)`            | NOT NULL, DEFAULT 'ACTIVE' | Account status (ACTIVE, BLOCKED)         |
| `created_at`   | `TIMESTAMP`              | NOT NULL                   | Account creation timestamp               |
| `updated_at`   | `TIMESTAMP`              |                            | Last update timestamp                    |

**Indexes:**
- `PRIMARY KEY (id)`
- `INDEX idx_customer_id (customer_id)` – for fast lookup by customer.

## Table `transactions`

Stores the history of all operations on accounts.

| Column               | Type                     | Constraints                                                       | Description                                                  |
|----------------------|--------------------------|-------------------------------------------------------------------|--------------------------------------------------------------|
| `id`                 | `UUID`                   | PRIMARY KEY                                                       | Unique transaction identifier                               |
| `account_id`         | `UUID`                   | NOT NULL, FOREIGN KEY (`account_id`) REFERENCES `accounts(id)`    | Account on which the operation was performed                |
| `amount`             | `DECIMAL(19,2)`          | NOT NULL                                                          | Transaction amount                                           |
| `type`               | `VARCHAR(20)`            | NOT NULL                                                          | Type: DEPOSIT, WITHDRAW, TRANSFER_SENT, TRANSFER_RECEIVED    |
| `related_account_id` | `UUID`                   | NULL                                                              | Counterparty account ID (for transfers)                      |
| `description`        | `TEXT`                   | NULL                                                              | Human-readable description                                   |
| `timestamp`          | `TIMESTAMP`              | NOT NULL                                                          | Operation timestamp                                          |

**Indexes:**
- `PRIMARY KEY (id)`
- `INDEX idx_account_id (account_id)` – for efficient pagination of account history.
- `INDEX idx_timestamp (timestamp)` – for sorting by time.

## Relationships

- `transactions.account_id` → `accounts.id` (many-to-one)
- One account can have many transactions.

## Entity Diagram (textual)
```
┌─────────────────┐     ┌──────────────────────┐
│ accounts        │     │ transactions         │
├─────────────────┤     ├──────────────────────┤
│ id (PK)         │◄────│ account_id (FK)      │
│ customer_id     │     │ id (PK)              │
│ balance         │     │ amount               │
│ currency        │     │ type                 │
│ status          │     │ related_account_id   │
│ created_at      │     │ description          │
│ updated_at      │     │ timestamp            │
└─────────────────┘     └──────────────────────┘
```


## Testing Notes

- Balance must never be negative – enforced by business logic (not at DB level).
- For transfers, two transactions are created: `TRANSFER_SENT` (on sender's account) and `TRANSFER_RECEIVED` (on recipient's account).
- `related_account_id` holds the counterparty account ID:
    - For `TRANSFER_SENT` – recipient ID.
    - For `TRANSFER_RECEIVED` – sender ID.
- Foreign key ensures referential integrity (transactions always reference existing accounts).