CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    user_id UUID,
    email VARCHAR(255),
    event_type VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    user_agent TEXT,
    trace_id VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_time ON audit_log(user_id, created_at);
CREATE INDEX idx_event_type ON audit_log(event_type);
CREATE INDEX idx_created_at ON audit_log(created_at);
CREATE INDEX idx_trace_id ON audit_log(trace_id);
