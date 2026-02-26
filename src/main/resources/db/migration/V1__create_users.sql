CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);