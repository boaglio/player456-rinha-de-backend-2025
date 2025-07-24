CREATE UNLOGGED TABLE IF NOT EXISTS pagamentos (
    id SERIAL PRIMARY KEY NOT NULL,
    amount REAL,
    dt_transaction TIMESTAMP DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
);

CREATE UNLOGGED TABLE IF NOT EXISTS pagamentos_fallback (
    id SERIAL PRIMARY KEY NOT NULL,
    amount REAL,
    dt_transaction TIMESTAMP DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
);

CREATE UNLOGGED TABLE IF NOT EXISTS pagamentos_retry (
    id SERIAL PRIMARY KEY NOT NULL,
    correlation_id CHAR(100),
    amount CHAR(100)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_correlation_id ON pagamentos_retry (correlation_id);

CREATE EXTENSION IF NOT EXISTS pg_prewarm;

SELECT pg_prewarm('pagamentos');

SELECT pg_prewarm('pagamentos_fallback');

SELECT pg_prewarm('pagamentos_retry');
