package com.expensetracker;

import java.time.LocalDate;

public class Expense {
    private int id;
    private double amount;
    private LocalDate date;
    private int categoryId;
    private String categoryName;
    private String note;

    public Expense(int id, double amount, LocalDate date,
                   int categoryId, String categoryName, String note) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.note = note;
    }

    public Expense(double amount, LocalDate date,
                   int categoryId, String categoryName, String note) {
        this(-1, amount, date, categoryId, categoryName, note);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
