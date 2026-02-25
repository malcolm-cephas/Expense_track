package com.expensetracker;

import java.time.LocalDate;

public class Expense {
    private int id;
    private double amount;
    private LocalDate date;
    private int categoryId;
    private String categoryName;
    private String project;
    private String paymentMethod; // NEW
    private boolean reimbursed; // NEW
    private String note;

    public Expense(int id, double amount, LocalDate date,
            int categoryId, String categoryName, String project,
            String paymentMethod, boolean reimbursed, String note) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.project = project;
        this.paymentMethod = paymentMethod;
        this.reimbursed = reimbursed;
        this.note = note;
    }

    // Constructor for new expenses (no ID yet)
    public Expense(double amount, LocalDate date,
            int categoryId, String categoryName, String project,
            String paymentMethod, boolean reimbursed, String note) {
        this(-1, amount, date, categoryId, categoryName, project, paymentMethod, reimbursed, note);
    }

    // Legacy support for older constructor
    public Expense(int id, double amount, LocalDate date,
            int categoryId, String categoryName, String project, String note) {
        this(id, amount, date, categoryId, categoryName, project, "Cash", false, note);
    }

    // Legacy constructor for backward compatibility
    public Expense(int id, double amount, LocalDate date,
            int categoryId, String categoryName, String note) {
        this(id, amount, date, categoryId, categoryName, null, "Cash", false, note);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isReimbursed() {
        return reimbursed;
    }

    public void setReimbursed(boolean reimbursed) {
        this.reimbursed = reimbursed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
