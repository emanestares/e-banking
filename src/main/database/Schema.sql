ALTER LOGIN [sa] ENABLE;
ALTER LOGIN [sa] WITH PASSWORD = 'Banking@123!';

CREATE DATABASE MiniBankingDB;

USE MiniBankingDB;
 
-- =====================
-- CLEAR EXISTING DATA (order matters due to foreign keys)
-- =====================
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;
 
-- Reset identity counters
DBCC CHECKIDENT ('users', RESEED, 0);
DBCC CHECKIDENT ('accounts', RESEED, 0);
DBCC CHECKIDENT ('transactions', RESEED, 0);
 
-- =====================
-- ROLES
-- =====================
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER');
SET IDENTITY_INSERT roles OFF;
 
-- Verify
 
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM user_roles;
SELECT * FROM accounts;
SELECT * FROM transactions;
