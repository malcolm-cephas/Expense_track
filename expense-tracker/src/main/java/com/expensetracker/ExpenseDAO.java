package com.expensetracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ExpenseDAO {

    // Get all categories as id -> name
    public static Map<Integer, String> getCategories() {
        Map<Integer, String> map = new LinkedHashMap<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 🔹 NEW: get existing category id or create a new one
    public static int getOrCreateCategoryId(String name) {
        if (name == null) throw new IllegalArgumentException("Category name is null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Category name is empty");

        try (Connection conn = Database.getConnection()) {
            // 1) Try to find existing
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM categories WHERE name = ?")) {
                ps.setString(1, trimmed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            // 2) Insert new
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO categories(name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, trimmed);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve new category id");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in getOrCreateCategoryId: " + e.getMessage(), e);
        }
    }

    public static List<Expense> getExpensesByMonthYear(int year, int month) {
        List<Expense> list = new ArrayList<>();
        String sql = """
                SELECT e.id, e.amount, e.date,
                       c.id AS cid, c.name AS cname,
                       e.note
                FROM expenses e
                JOIN categories c ON e.category_id = c.id
                WHERE strftime('%Y', e.date) = ?
                  AND strftime('%m', e.date) = ?
                ORDER BY e.date
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.format("%04d", year));
            ps.setString(2, String.format("%02d", month));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    double amount = rs.getDouble("amount");
                    LocalDate date = LocalDate.parse(rs.getString("date"));
                    int cid = rs.getInt("cid");
                    String cname = rs.getString("cname");
                    String note = rs.getString("note");
                    list.add(new Expense(id, amount, date, cid, cname, note));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertExpense(Expense e) {
        String sql = """
                INSERT INTO expenses (amount, date, category_id, note)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, e.getAmount());
            ps.setString(2, e.getDate().toString());
            ps.setInt(3, e.getCategoryId());
            ps.setString(4, e.getNote());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateExpense(Expense e) {
        String sql = """
                UPDATE expenses
                SET amount = ?, date = ?, category_id = ?, note = ?
                WHERE id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, e.getAmount());
            ps.setString(2, e.getDate().toString());
            ps.setInt(3, e.getCategoryId());
            ps.setString(4, e.getNote());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String, Double> getMonthlyTotalsByCategory(int year, int month) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
                SELECT c.name, SUM(e.amount) AS total
                FROM expenses e
                JOIN categories c ON e.category_id = c.id
                WHERE strftime('%Y', e.date) = ?
                  AND strftime('%m', e.date) = ?
                GROUP BY c.name
                ORDER BY total DESC
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.format("%04d", year));
            ps.setString(2, String.format("%02d", month));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<LocalDate, Double> getDailyTotals(int year, int month) {
        Map<LocalDate, Double> map = new LinkedHashMap<>();
        String sql = """
                SELECT date, SUM(amount) AS total
                FROM expenses
                WHERE strftime('%Y', date) = ?
                  AND strftime('%m', date) = ?
                GROUP BY date
                ORDER BY date
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.format("%04d", year));
            ps.setString(2, String.format("%02d", month));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = LocalDate.parse(rs.getString("date"));
                    map.put(d, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
