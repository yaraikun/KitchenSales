-- File: 03_app_queries.sql
-- Description: A collection of SQL queries for the application's core logic.

-- =================================================================
-- Section 1: Authentication
-- =================================================================
-- Check user credentials and return their role
SELECT role FROM staff WHERE username = ? AND password = ?;

-- =================================================================
-- Section 2: Data Retrieval for UI
-- =================================================================
-- Get all active discounts to display in a list/dropdown
SELECT discount_id, discount_name, type, value FROM discounts WHERE status = 'Active';

-- =================================================================
-- Section 3: Core Transactions
-- =================================================================
-- Transaction: Creating a POS Transaction & Kitchen Route
-- a. Create the POS Transaction header
INSERT INTO pos_transactions (transaction_date, terminal_id, total_amount, discount_amount, tax_amount, net_amount, status, payment_status)
VALUES (NOW(), ?, ?, ?, ?, ?, 'Active', 'Unpaid');

-- b. Create the POS Kitchen Order
INSERT INTO pos_kitchen_orders (transaction_id, status, created_time)
VALUES (?, 'Pending', NOW());

-- c. Insert each item into POS Transaction Lines
INSERT INTO pos_transaction_lines (transaction_id, kitchen_order_id, item_name, quantity, unit_price, line_total)
VALUES (?, ?, ?, ?, ?, ?);

-- Transaction: Process Payment
-- a. Create the payment record
INSERT INTO payments (transaction_id, payment_method, amount, payment_date)
VALUES (?, ?, ?, NOW());

-- b. Update the main transaction status to close it
UPDATE pos_transactions SET status = 'Completed', payment_status = 'Paid' WHERE transaction_id = ?;

-- Transaction: Apply Discount
-- a. Update the transaction with discount details
UPDATE pos_transactions SET discount_id = ?, discount_amount = ?, tax_amount = ?, net_amount = ? WHERE transaction_id = ?;

-- Transaction: Update Kitchen Order Status
-- a. Update status to 'Preparing'
UPDATE pos_kitchen_orders SET status = 'Preparing' WHERE kitchen_order_id = ? AND status = 'Pending';

-- b. Update status to 'Ready' and log completion time
UPDATE pos_kitchen_orders SET status = 'Ready', completed_time = NOW() WHERE kitchen_order_id = ? AND status = 'Preparing';

-- Transaction: Void an Order
-- a. Update transaction status
UPDATE pos_transactions SET status = 'Voided' WHERE transaction_id = ?;

-- b. Update kitchen order status
UPDATE pos_kitchen_orders SET status = 'Cancelled' WHERE transaction_id = ?;

-- --- End of Script ---
