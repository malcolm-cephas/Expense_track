package com.expensetracker;

import java.sql.*;

public class Database {

    // Use a fixed location in User Home so the DB is found regardless of where the
    // app is launched from
    private static final String DB_FOLDER = System.getProperty("user.home") + "/.expense-tracker";
    private static final String URL = "jdbc:sqlite:" + DB_FOLDER + "/expenses.db";

    public static Connection getConnection() throws SQLException {
        org.sqlite.SQLiteConfig config = new org.sqlite.SQLiteConfig();
        config.enforceForeignKeys(true); // Enable foreign keys constraints
        return DriverManager.getConnection(URL, config.toProperties());
    }

    public static void init() {
        // Ensure the directory exists
        java.io.File directory = new java.io.File(DB_FOLDER);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created && !directory.exists()) {
                throw new RuntimeException("Failed to create application data directory: " + DB_FOLDER);
            }
        }

        try (Connection conn = getConnection();
                Statement st = conn.createStatement()) {

            // Categories table
            st.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE
                    )
                    """);

            // Expenses table
            st.execute("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        amount REAL NOT NULL,
                        date   TEXT NOT NULL,        -- YYYY-MM-DD
                        category_id INTEGER NOT NULL,
                        project TEXT,                -- New column for Client/Project
                        payment_method TEXT,         -- NEW: Cash, Card, etc.
                        reimbursed INTEGER DEFAULT 0, -- NEW: 0=No, 1=Yes
                        note   TEXT,
                        FOREIGN KEY (category_id) REFERENCES categories(id)
                    )
                    """);

            // Attempt to add new columns to existing tables if missing
            try {
                st.execute("ALTER TABLE expenses ADD COLUMN project TEXT");
            } catch (SQLException e) {
            }
            try {
                st.execute("ALTER TABLE expenses ADD COLUMN payment_method TEXT");
            } catch (SQLException e) {
            }
            try {
                st.execute("ALTER TABLE expenses ADD COLUMN reimbursed INTEGER DEFAULT 0");
            } catch (SQLException e) {
            }

            // Insert default BUSINESS categories if table is empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String[] defaults = {
                            "Office Supplies", "Travel & Transport", "Meals & Entertainment",
                            "Software & Subscriptions", "Rent & Utilities", "Professional Services",
                            "Marketing & Ads", "Payroll", "Equipment", "Other"
                    };
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO categories(name) VALUES (?)")) {
                        for (String c : defaults) {
                            ps.setString(1, c);
                            ps.executeUpdate();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Log to console
            // Re-throw so the UI knows to stop
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
}
