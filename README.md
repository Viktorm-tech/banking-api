# Banking API

A simplified banking API built with **Spring Boot 4.0**, **Java 25**, **PostgreSQL**, and **Kafka**.  
The service supports account management, deposits, withdrawals (with limit checking via an external mock service), transfers, and paginated transaction history.

## Features

- Create, retrieve, and update bank accounts
- Deposit / withdraw funds (withdrawals respect balance and external limit checks)
- Transfer money between accounts
- Paginated transaction history per account
- Kafka events published for account creation and transfers (`account-events` topic)
- External limit check service (e.g., WireMock) – configured via `LIMITS_CHECK_URL`

## Architecture

- **PostgreSQL** – persistent storage for accounts and transactions
- **Kafka + Zookeeper** – event streaming
- **External Limits Service** – called on withdrawal/transfer to verify amount limits (e.g., `{"allowed": true}` for amounts < 10000)

> **Note:** The WireMock container is **not** part of the main `docker-compose.yml`. It is meant to be started separately (e.g., from a test repository) to provide mock responses for the limits endpoint.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- (Optional) Java 25 / Maven for local development

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/your-repo/banking-api.git
cd banking-api
```

### 2. Start the core services (PostgreSQL, Kafka, Zookeeper, and the banking API)

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### 3. (Optional) Run the external WireMock container for limit mocking

If you want to test the limit‑checking behaviour, run WireMock separately:

```bash
docker run -d --name wiremock -p 8081:8080 wiremock/wiremock:3.9.1
```

Then reconfigure the banking API to use this WireMock instance:

```bash
docker-compose up -d --force-recreate -e LIMITS_CHECK_URL=http://host.docker.internal:8081/limits/check
```

> On Linux, replace `host.docker.internal` with your host IP (e.g., `172.17.0.1`).

## Environment Variables

You can override the following variables in `docker-compose.yml` or via the command line:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/bankingdb` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `bankinguser` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `bankingpass` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `kafka:9092` |
| `LIMITS_CHECK_URL` | External limits service endpoint | `http://wiremock:8080/limits/check` |

## API Endpoints

All endpoints return JSON. Error responses follow the format:

```json
{
"message": "Error description",
"status": 400,
"timestamp": "2026-04-14T10:00:00"
}
```

### 1. Create an account

**POST** `/api/accounts`

Request body:
```json
{
"customerId": "cust123",
"initialBalance": 1000.00,
"currency": "USD"
}
```

Response `201 Created`:
```json
{
"id": "550e8400-e29b-41d4-a716-446655440000",
"customerId": "cust123",
"balance": 1000.00,
"currency": "USD",
"status": "ACTIVE"
}
```

### 2. Get account details

**GET** `/api/accounts/{id}`

Response `200 OK` (same as above).  
`404 Not Found` if the account does not exist.

### 3. Deposit funds

**POST** `/api/accounts/{id}/deposit`

Request body:
```json
{
"amount": 500.00
}
```

Response `200 OK` (no body).

### 4. Withdraw funds

**POST** `/api/accounts/{id}/withdraw`

Request body:
```json
{
"amount": 300.00
}
```

Response `200 OK` if balance is sufficient and the external limit check passes.  
`400 Bad Request` if insufficient balance or limit exceeded.

### 5. Transfer between accounts

**POST** `/api/accounts/{fromId}/transfer`

Request body:
```json
{
"toAccountId": "550e8400-e29b-41d4-a716-446655440001",
"amount": 200.00
}
```

Response `200 OK` on success.  
`400 Bad Request` for insufficient funds, limit violation, or self‑transfer.

### 6. Transaction history (paginated)

**GET** `/api/accounts/{id}/transactions?page=0&size=10`

Response `200 OK` with a paginated list:

```json
{
"content": [
{
"id": "tx1",
"amount": 1000.00,
"type": "DEPOSIT",
"relatedAccountId": null,
"description": "Initial deposit",
"timestamp": "2026-04-14T10:00:00"
}
],
"pageable": { },
"totalPages": 1,
"totalElements": 1
}
```

## Example `curl` Commands

```bash
# Create account
curl -X POST http://localhost:8080/api/accounts \
-H "Content-Type: application/json" \
-d "{\"customerId\":\"user1\",\"initialBalance\":5000,\"currency\":\"USD\"}"

# Get account (replace <id>)
curl http://localhost:8080/api/accounts/<id>

# Deposit
curl -X POST http://localhost:8080/api/accounts/<id>/deposit \
-H "Content-Type: application/json" \
-d "{"amount":1000}"

# Withdraw (limit check passes if amount < 10000)
curl -X POST http://localhost:8080/api/accounts/<id>/withdraw \
-H "Content-Type: application/json" \
-d "{"amount":500}"

# Transfer
curl -X POST http://localhost:8080/api/accounts/<from>/transfer \
-H "Content-Type: application/json" \
-d "{"toAccountId":"<to>","amount":300}"

# Transaction history
curl "http://localhost:8080/api/accounts/<id>/transactions?page=0&size=10"
```

## Running Integration Tests with WireMock

The banking API expects an external service at `LIMITS_CHECK_URL`. For automated tests you can spin up a dedicated WireMock container and point the API to it.

## Logging

Logs are printed to the console. To increase verbosity, set:

```bash
LOGGING_LEVEL_COM_EXAMPLE_BANKING=DEBUG
```

in the environment or inside `application.yml`.

## Technologies

- Spring Boot 4.0
- Java 25
- Spring Data JPA + Hibernate
- PostgreSQL 15
- Apache Kafka
- Flyway (migrations)
- Maven

## License

MIT