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


-- =====================
-- SYSTEM TRANSACTION CONFIGURATION LIMITERS
-- =====================
INSERT INTO limiters (limiter_key, limiter_value, description) VALUES
('starterAccountLimit', '50000.00', 'Maximum initial opening deposit limit constraint allowed for anonymous signups.'),
('maxTransferAmount', '100000.00', 'Maximum processing transaction value ceiling allowed for a single fund transfer execution.');

-- =====================
-- USERS
-- =====================
INSERT INTO users (username, email, full_name, password_hash, is_active, created_at, updated_at) VALUES
('admin',  'admin@banking.com',  'System Administrator', '$2a$12$YyInU06eWRc44K5/UNc.yuMWoAT8BW3010e8gesIE6DXzkN17Ovs2', 1, GETDATE(), GETDATE()),
('jdoe',   'jdoe@banking.com',   'John Doe',             '$2a$12$92hMvFTMCzrTEV1qPYBJFON7P.HGBH5RlFpMLi1GU5k7r4VGMPsOe', 1, GETDATE(), GETDATE()),
('jsmith', 'jsmith@banking.com', 'Jane Smith',           '$2a$12$92hMvFTMCzrTEV1qPYBJFON7P.HGBH5RlFpMLi1GU5k7r4VGMPsOe', 1, GETDATE(), GETDATE());
 
-- =====================
-- USER_ROLES
-- =====================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'jdoe'   AND r.name = 'ROLE_USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'jsmith' AND r.name = 'ROLE_USER';
 
-- =====================
-- ACCOUNTS
-- =====================
INSERT INTO accounts (account_number, account_type, balance, is_active, user_id, created_at, updated_at)
SELECT 'ACC-0000000001', 'SAVINGS',  50000.00, 1, id, GETDATE(), GETDATE() FROM users WHERE username = 'jdoe';
INSERT INTO accounts (account_number, account_type, balance, is_active, user_id, created_at, updated_at)
SELECT 'ACC-0000000002', 'CHECKING', 25000.00, 1, id, GETDATE(), GETDATE() FROM users WHERE username = 'jdoe';
INSERT INTO accounts (account_number, account_type, balance, is_active, user_id, created_at, updated_at)
SELECT 'ACC-0000000003', 'SAVINGS',  75000.00, 1, id, GETDATE(), GETDATE() FROM users WHERE username = 'jsmith';
INSERT INTO accounts (account_number, account_type, balance, is_active, user_id, created_at, updated_at)
SELECT 'ACC-0000000004', 'CHECKING', 10000.00, 1, id, GETDATE(), GETDATE() FROM users WHERE username = 'jsmith';
 
-- =====================
-- TRANSACTIONS
-- =====================
INSERT INTO transactions (reference_number, transaction_type, amount, status, sender_account_id, receiver_account_id, description, created_at)
VALUES ('TXN-20260505-0001', 'TRANSFER', 5000.00, 'COMPLETED',
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000001'),
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000003'),
    'Transfer to Jane savings', GETDATE());
 
INSERT INTO transactions (reference_number, transaction_type, amount, status, sender_account_id, receiver_account_id, description, created_at)
VALUES ('TXN-20260505-0002', 'TRANSFER', 2000.00, 'COMPLETED',
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000003'),
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000002'),
    'Transfer to John checking', GETDATE());
 
INSERT INTO transactions (reference_number, transaction_type, amount, status, sender_account_id, receiver_account_id, description, created_at)
VALUES ('TXN-20260505-0003', 'DEPOSIT', 10000.00, 'COMPLETED', NULL,
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000001'),
    'Initial deposit', GETDATE());
 
INSERT INTO transactions (reference_number, transaction_type, amount, status, sender_account_id, receiver_account_id, description, created_at)
VALUES ('TXN-20260505-0004', 'WITHDRAWAL', 1500.00, 'COMPLETED',
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000002'),
    NULL, 'ATM withdrawal', GETDATE());
 
INSERT INTO transactions (reference_number, transaction_type, amount, status, sender_account_id, receiver_account_id, description, created_at)
VALUES ('TXN-20260505-0005', 'TRANSFER', 3000.00, 'PENDING',
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000001'),
    (SELECT id FROM accounts WHERE account_number = 'ACC-0000000004'),
    'Pending transfer', GETDATE());
 
-- Verify
 
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM user_roles;
SELECT * FROM accounts;
SELECT * FROM transactions;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admirers' AND r.name = 'ROLE_ADMIN';

UPDATE accounts 
SET balance = 1000000.00, 
    updated_at = GETDATE() 
WHERE account_number = 'ACC-72023';