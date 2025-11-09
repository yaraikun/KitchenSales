# Korean BBQ Point-of-Sale (POS) System

This project is a database-driven Point-of-Sale (POS) application for a Korean
BBQ restaurant, developed for the CCINFOM S18 course.

**Team Members:**
*   BUENO, Christian Josh M.
*   CALUPIG, Evan Riley L.
*   CONDUCTO, Thaeron Sean M.
*   LUTZ, Shawn R.

---

## Project Structure

```
Korean_BBQ_POS_App/
├── .gitignore
├── README.md
├── lib/
│   └── mysql-connector-j-8.4.0.jar  // Whatever version we need for 8.0.43
│
├── sql/
│   ├── 01_create_database.sql
│   ├── 02_create_tables.sql
│   ├── 03_app_queries.sql
│   └── 04_report_queries.sql
│
└── src/                             // Idk ye
    └── com/
        └── dlsu/
            └── ccinfom/
                └── pos/
                    ├── Main.java
                    ├── model/
                    ├── view/
                    └── database/
```

---

## Setup

### Tech Stack
*   **Java Development Kit (JDK):** Version 21 (LTS) or higher.
*   **MySQL Server:** Version 8.0.43.
*   **IDE:** IntelliJ IDEA.
*   **SQL Client:** MySQL Workbench.

### Database Setup

1.  Open MySQL Workbench and connect to your local MySQL server.
2.  Run the script `sql/01_create_database.sql` to create the `korean_bbq_pos`
    database.
3.  Run the script `sql/02_create_tables.sql` to create all the necessary
    tables.

### Project Setup
1.  Clone the GitHub repository to your local machine.
2.  Open the project folder in IntelliJ IDEA.
3.  Add the MySQL JDBC driver (the `.jar` file in the `lib/` folder) to your
    project's dependencies.
4.  Configure the database connection details (URL, username, password) in the
    `DatabaseConnector.java` class.

---

## The Game Plan

### **SQL Scripts:**

*   **`01_create_database.sql`**: Creates the database schema.

*   **`02_create_tables.sql`**: Contains all `CREATE TABLE` statements with
correct data types, primary keys, and foreign keys.

*   **`03_app_queries.sql`**: A collection of all `INSERT`, `UPDATE`, and
`SELECT` statements the application will use for its core functions.

*   **`04_report_queries.sql`**: Contains the more complex `SELECT` statements
with joins and aggregate functions needed for the reporting module.

### **UI and App:**

#### **List of App Pages:**

1.  **Login Screen:**
    *   Input fields for username and password.
    *   Authenticates user and directs them to the appropriate screen (Cashier
    or Manager).

2.  **Point of Sale (POS) Main Screen (For Cashiers):**
    *   **Menu Grid:** Clickable buttons for all menu items.
    *   **Current Order Panel:** A live-updating list of items added to the
    current sale, showing quantity, price, and subtotal.
    *   **Action Buttons:**
        *   `Send to Kitchen`: Finalizes the item list and creates a
        `pos_kitchen_order`.
        *   `Apply Discount`: Opens a dialog to select and apply a discount.
        *   `Pay`: Proceeds to the Payment Dialog.
        *   `Void Order`: Cancels the current transaction.

3.  **Payment Dialog:**
    *   Displays the final amount due.
    *   Buttons for payment methods (`Cash`, `Card`, `E-Wallet`).
    *   Input for cash amount received and calculation of change.
    *   `Confirm Payment` button to finalize the sale.

4.  **Reports Dashboard (For Managers):**
    *   A navigation panel to select a report.
    *   Date pickers to filter data by a specific time frame.
    *   A table view to display the results of the selected report.

5.  **Kitchen Display System (KDS):**
    *   A simple, auto-refreshing screen showing active kitchen orders.
    *   Each order will have a button to update its status from `Pending` ->
    `Preparing` -> `Ready`.
