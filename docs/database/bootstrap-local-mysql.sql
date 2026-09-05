-- =====================================================================================
-- One-time local bootstrap: create the database and the application user.
-- =====================================================================================
-- Run this ONCE, as root, before starting the application for the first time against a
-- locally installed MySQL (i.e. when you are not using docker-compose, which provisions
-- the database and user automatically from its environment variables).
--
-- From the project root:
--
--   bash / Git Bash / macOS / Linux
--     mysql -u root -p < docs/database/bootstrap-local-mysql.sql
--
--   Windows PowerShell (PowerShell has no '<' redirection, so pipe the file in)
--     Get-Content docs\database\bootstrap-local-mysql.sql |
--         & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
--
--   Or open this file in MySQL Workbench, connect as root, and execute it.
--
-- It creates only the empty database and the account. It does NOT create any tables:
-- Flyway does that on first startup by applying V1..V4 from
-- src/main/resources/db/migration (plus the dev seed data under db/seed).
--
-- The credentials below match the defaults in application-dev.properties, so the app
-- runs with no environment variables set. They are development-only — override
-- DB_USERNAME / DB_PASSWORD in any other environment.
-- =====================================================================================

CREATE DATABASE IF NOT EXISTS travel_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- caching_sha2_password is MySQL 8's default and is what mysql-connector-j 9.x expects;
-- the JDBC URL already sets allowPublicKeyRetrieval=true&useSSL=false so the handshake
-- works over a plaintext local connection.
CREATE USER IF NOT EXISTS 'travel_user'@'localhost'
    IDENTIFIED WITH caching_sha2_password BY 'travel_pass';

-- Flyway needs DDL rights (CREATE/ALTER/DROP/INDEX/REFERENCES) in addition to the DML the
-- application uses, so grant the whole schema rather than enumerating privileges.
GRANT ALL PRIVILEGES ON travel_platform.* TO 'travel_user'@'localhost';

FLUSH PRIVILEGES;

-- Sanity check — should list the new account and the database.
SELECT user, host, plugin FROM mysql.user WHERE user = 'travel_user';
SHOW DATABASES LIKE 'travel_platform';
