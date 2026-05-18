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


IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[limiters]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[limiters] (
    [id] BIGINT IDENTITY(1,1) NOT NULL,
    [limiter_key] VARCHAR(100) NOT NULL UNIQUE,
    [limiter_value] VARCHAR(255) NOT NULL,
    [description] VARCHAR(500) NULL,
    CONSTRAINT [PK_limiters] PRIMARY KEY CLUSTERED ([id] ASC)
    );
END

 -- =====================
-- SYSTEM TRANSACTION CONFIGURATION LIMITERS
-- =====================
INSERT INTO limiters (limiter_key, limiter_value, description) VALUES
('starterAccountLimit', '50000.00', 'Maximum initial opening deposit limit constraint allowed for anonymous signups.'),
('maxTransferAmount', '100000.00', 'Maximum processing transaction value ceiling allowed for a single fund transfer execution.');
 
-- Verify
 
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM user_roles;
SELECT * FROM accounts;
SELECT * FROM transactions;
SELECT * FROM limiters;
