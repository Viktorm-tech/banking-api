CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    amount DECIMAL(19,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    related_account_id UUID,
    description TEXT,
    timestamp TIMESTAMP NOT NULL
);