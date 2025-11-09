-- File: 03_app_queries.sql
-- Description: A collection of SQL queries for the application's core
--              transactional logic.

-- =================================================================
-- Transaction: Creating a POS Transaction & Kitchen Route
-- =================================================================
-- a. Create the POS Transaction header
INSERT INTO pos_transactions (transaction_date, terminal_id, total_amount, discount_amount, tax_amount, net_amount, status, payment_status)
VALUES (NOW(), ?, ?, ?, ?, ?, 'Active', 'Unpaid');

-- b. Create the POS Kitchen Order
INSERT INTO pos_kitchen_orders (transaction_id, status, created_time)
VALUES (?, 'Pending', NOW()); 

-- c. Insert each item into POS Transaction Lines
INSERT INTO pos_transaction_lines (transaction_id, kitchen_order_id, item_name, quantity, unit_price, line_total)
VALUES (?, ?, ?, ?, ?, ?);

-- =================================================================
-- Transaction: Process Payment
-- =================================================================

-- a. Create the payment record
INSERT INTO payments (transaction_id, payment_method, amount, payment_date)
VALUES (?, ?, ?, NOW());

-- b. Update the main transaction status to close it
UPDATE pos_transactions
SET status = 'Completed', payment_status = 'Paid'
WHERE transaction_id = ?;


-- =================================================================
-- Transaction: Apply Discount 
-- =================================================================

-- a. Update the transaction with discount details
UPDATE pos_transactions
SET 
    discount_amount = ?,
    tax_amount = ?,
    net_amount = ?
WHERE transaction_id = ?;

-- =================================================================
-- Transaction: Update Kitchen Order Status
-- =================================================================

-- a. Update the status and completed time of a kitchen order
UPDATE pos_kitchen_orders
SET status = ?
WHERE kitchen_order_id = ?;

UPDATE pos_kitchen_orders
SET 
    status = 'Ready',
    completed_time = NOW()
WHERE kitchen_order_id = ? AND status = 'Preparing';
