-- File: 04_report_queries.sql
-- Description: A collection of SQL queries for generating business reports.

-- =================================================================
-- Report: Sales Summary Report
-- =================================================================
SELECT 
    DATE(transaction_date) AS sale_date,
    COUNT(*) AS total_transactions,
    SUM(net_amount) AS total_sales,
    AVG(net_amount) AS average_sales
FROM pos_transactions
WHERE 
    YEAR(transaction_date) = ? AND
    MONTH(transaction_date) = ?
    AND status = 'Completed'
GROUP BY sale_date
ORDER BY sale_date;


-- =================================================================
-- Report: Product Sales Performance Report
-- =================================================================
SELECT 
    item_name,
    SUM(quantity) AS total_quantity_sold,
    SUM(line_total) AS total_revenue
FROM pos_transaction_lines AS ptl
JOIN pos_transactions AS pt ON ptl.transaction_id = pt.transaction_id
WHERE 
    YEAR(pt.transaction_date) = ? AND
    MONTH(pt.transaction_date) = ?
    AND pt.status = 'Completed'
GROUP BY item_name
ORDER BY total_revenue DESC;

-- =================================================================
-- Report: Discount Utilization Report
-- =================================================================
-- Add query here.


-- =================================================================
-- Report: Payment Method Report
-- =================================================================
-- Add query here.


-- =================================================================
-- Report: Kitchen Efficiency Report
-- =================================================================
-- Add query here.


-- --- End of Script ---
