# Kafka Event Schema

## Topic: `account-events`

This topic is used to publish domain events related to bank accounts.

### Event Types

| Event Type           | Description                              | When published                          |
|----------------------|------------------------------------------|-----------------------------------------|
| `ACCOUNT_CREATED`    | A new bank account was created           | After successful account creation       |
| `TRANSFER_COMPLETED` | Money was transferred between accounts   | After a successful transfer operation   |

### Event Schema (JSON)

All events share a common envelope with a type-specific `data` field.

#### Common Envelope

| Field               | Type        | Description                                             |
|---------------------|-------------|---------------------------------------------------------|
| `eventType`         | `string`    | One of the event types listed above                     |
| `accountId`         | `string`    | UUID of the primary account (creator or sender)         |
| `relatedAccountId`  | `string`    | UUID of the related account (only for transfers)        |
| `data`              | `object`    | Event-specific payload (see below)                      |
| `timestamp`         | `string`    | ISO-8601 timestamp (e.g., `2026-04-26T10:00:00Z`)       |

#### Event-Specific Payloads

**Event type `ACCOUNT_CREATED`**

The `data` object contains:

| Field            | Type      | Description                          |
|------------------|-----------|--------------------------------------|
| `customerId`     | `string`  | Customer identifier                  |
| `initialBalance` | `number`  | Initial account balance              |
| `currency`       | `string`  | Currency code (e.g., `USD`, `EUR`)   |

Example:
```json
{
  "eventType": "ACCOUNT_CREATED",
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "relatedAccountId": null,
  "data": {
    "customerId": "cust123",
    "initialBalance": 1000.00,
    "currency": "USD"
  },
  "timestamp": "2026-04-26T12:34:56.123Z"
}
```

**Event type `TRANSFER_COMPLETED`**

The `data` object contains:

| Field            | Type      | Description                    |
|------------------|-----------|--------------------------------|
| `amount`         | `number`  | Transferred amount             |
| `toAccount`      | `string`  | UUID of the recipient account  |

Example:
```json
{
  "eventType": "TRANSFER_COMPLETED",
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "relatedAccountId": "660f9511-f33c-52e5-b827-557766551111",
  "data": {
    "amount": 250.00,
    "toAccount": "660f9511-f33c-52e5-b827-557766551111"
  },
  "timestamp": "2026-04-26T12:35:00.456Z"
}
```

## Configuration

- **Topic name:** `account-events`
- **Partitions:** 1 (default, can be changed)
- **Replication factor:** 1 (suitable for development)
- **Retention:** 7 days (default)

## Consumer Notes

- Events are published **after** the corresponding database transaction commits.
- Ordering is guaranteed per account ID (used as Kafka message key).
- Consumers should handle idempotency – the same event may be delivered more than once in case of retries.