-- Run using psql as root; shared compose initializes these same credentials.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bookadmin') THEN
        CREATE ROLE bookadmin LOGIN PASSWORD 'bookadmin';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'theuser') THEN
        CREATE ROLE theuser LOGIN PASSWORD 'theuser';
    END IF;
END $$;

SELECT 'CREATE DATABASE booksdb OWNER bookadmin'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'booksdb')\gexec

\connect booksdb
GRANT CONNECT ON DATABASE booksdb TO theuser;
GRANT USAGE ON SCHEMA public TO theuser;
ALTER DEFAULT PRIVILEGES FOR ROLE bookadmin IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO theuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO theuser;
