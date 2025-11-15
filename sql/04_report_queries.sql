-- File: 04_report_queries.sql
-- Description: A collection of SQL queries for generating business reports.

-- =================================================================
-- Report: Sales Summary Report
-- =================================================================
SELECT
    DATE(transaction_date) AS sale_date,
    COUNT(transaction_id) AS total_transactions,
    SUM(net_amount) AS total_sales
FROM pos_transactions
WHERE status = 'Completed' AND transaction_date BETWEEN ? AND ?
GROUP BY sale_date
ORDER BY sale_date;

-- =================================================================
-- Report: Product Sales Performance Report
-- =================================================================
SELECT
    ptl.item_name,
    SUM(ptl.quantity) AS total_quantity_sold,
    SUM(ptl.line_total) AS total_revenue
FROM pos_transaction_lines ptl
JOIN pos_transactions pt ON ptl.transaction_id = pt.transaction_id
WHERE pt.status = 'Completed' AND pt.transaction_date BETWEEN ? AND ?
GROUP BY ptl.item_name
ORDER BY total_revenue DESC;

-- =================================================================
-- Report: Discount Utilization Report
-- =================================================================
SELECT
    d.discount_name,
    COUNT(t.transaction_id) AS times_used,
    SUM(t.discount_amount) AS total_value
FROM pos_transactions t
JOIN discounts d ON t.discount_id = d.discount_id
WHERE t.status = 'Completed' AND t.transaction_date BETWEEN ? AND ?
GROUP BY d.discount_name
ORDER BY times_used DESC;

-- =================================================================
-- Report: Payment Method Report
-- =================================================================
SELECT
    payment_method,
    COUNT(payment_id) AS number_of_transactions,
    SUM(amount) AS total_collected
FROM payments p
JOIN pos_transactions t ON p.transaction_id = t.transaction_id
WHERE t.status = 'Completed' AND p.payment_date BETWEEN ? AND ?
GROUP BY payment_method
ORDER BY total_collected DESC;

-- =================================================================
-- Report: Kitchen Efficiency Report
-- =================================================================
SELECT
    DATE(created_time) AS order_date,
    COUNT(kitchen_order_id) AS total_orders,
    AVG(TIMESTAMPDIFF(MINUTE, created_time, completed_time)) AS avg_prep_time_minutes
FROM pos_kitchen_orders
WHERE status = 'Ready' AND completed_time IS NOT NULL AND created_time BETWEEN ? AND ?
GROUP BY order_date
ORDER BY order_date;

-- --- End of Script ---
