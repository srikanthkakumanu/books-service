DROP DATABASE IF EXISTS booksdb;
DROP USER IF EXISTS `bookadmin`@`%`;
DROP USER IF EXISTS `theuser`@`%`;
CREATE DATABASE IF NOT EXISTS booksdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS `bookadmin`@`%` IDENTIFIED BY 'bookadmin';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW,
CREATE ROUTINE, ALTER ROUTINE, EVENT, TRIGGER ON `booksdb`.* TO `bookadmin`@`%` WITH GRANT OPTION;
-- GRANT ALL ON `booksdb`.* TO `bookadmin`@`%` WITH GRANT OPTION;
CREATE USER IF NOT EXISTS `theuser`@`%` IDENTIFIED BY 'theuser';
GRANT SELECT, INSERT, UPDATE, DELETE, SHOW VIEW ON `booksdb`.* TO `theuser`@`%`;
-- GRANT ALL ON `booksdb`.* TO `theuser`@`%` WITH GRANT OPTION;
FLUSH PRIVILEGES;