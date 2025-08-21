-- This script is run automatically by the PostgreSQL container on initialization.
-- It is executed by the 'booksadmin' user against the 'booksdb' database, as configured in compose.yml.
-- The Docker entrypoint handles creating the database and 'booksadmin' role.

-- Create the 'booksdb' database if it doesn't exist.
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_database
      WHERE  datname = 'booksdb') THEN
      CREATE DATABASE booksdb;
   END IF;
END
$do$;

-- Create the 'booksadmin' role if it doesn't exist.
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'booksadmin') THEN
      CREATE ROLE booksadmin WITH LOGIN PASSWORD 'booksadmin';
   END IF;
END
$do$;

-- Create the application user role 'theuser' if it doesn't exist.
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'theuser') THEN

      CREATE ROLE theuser WITH LOGIN PASSWORD 'theuser';
   END IF;
END
$do$;

-- Grant 'theuser' the ability to connect to the database.
GRANT CONNECT ON DATABASE booksdb TO theuser;

-- Grant privileges on the 'public' schema, where Flyway will create tables.
-- USAGE: Allows 'theuser' to access objects in the schema.
-- CREATE: Allows 'theuser' to create (and drop) its own tables, covering the CREATE/DROP requirement.
GRANT USAGE, CREATE ON SCHEMA public TO theuser;

-- Grant DML privileges to 'theuser' for future tables and sequences created by 'booksadmin' (via Flyway).
-- This covers SELECT, INSERT, UPDATE, DELETE. The SELECT privilege also covers the intent of MySQL's `SHOW VIEW`.
ALTER DEFAULT PRIVILEGES FOR ROLE booksadmin IN SCHEMA public
   GRANT SELECT, INSERT, UPDATE, ALTER, DELETE ON TABLES TO theuser;
ALTER DEFAULT PRIVILEGES FOR ROLE booksadmin IN SCHEMA public
   GRANT USAGE, SELECT ON SEQUENCES TO theuser;
