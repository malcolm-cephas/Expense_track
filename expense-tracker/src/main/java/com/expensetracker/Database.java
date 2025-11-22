package com.expensetracker;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:sqlite:expenses.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
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
                        note   TEXT,
                        FOREIGN KEY (category_id) REFERENCES categories(id)
                    )
                    """);

            // Insert some default categories if table is empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String[] defaults = {
                            "Food", "Transport", "Rent",
                            "Shopping", "Bills", "Entertainment", "Other"
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
            e.printStackTrace();
        }
    }
}
