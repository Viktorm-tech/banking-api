CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    balance DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);