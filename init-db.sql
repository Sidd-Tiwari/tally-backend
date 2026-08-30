-- Runs automatically the first time the Postgres container starts.
-- Creates one database per microservice (database-per-service pattern).
CREATE DATABASE auth_db;
CREATE DATABASE expense_db;
CREATE DATABASE habit_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO tracker;
GRANT ALL PRIVILEGES ON DATABASE expense_db TO tracker;
GRANT ALL PRIVILEGES ON DATABASE habit_db TO tracker;
