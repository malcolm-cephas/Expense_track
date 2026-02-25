package com.expensetracker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class Main extends Application {

    private TableView<Expense> table;

    // Raw data from DB
    private final ObservableList<Expense> masterData = FXCollections.observableArrayList();
    // For search
    private FilteredList<Expense> filteredData;

    private ComboBox<Integer> yearBox;
    private ComboBox<Month> monthBox;
    private DatePicker fromDatePicker; // NEW
    private DatePicker toDatePicker; // NEW
    private TextField searchField;
    private Label totalLabel;
    private VBox dashboardBox; // NEW: Summary Panel

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    public void start(Stage primaryStage) {
        try {
            Database.init(); // ensure DB + tables + categories
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database initialization failed:\n" + e.getMessage(),
                    ButtonType.OK);
            alert.showAndWait();
            Platform.exit();
            return;
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topBar = buildTopBar(primaryStage);
        root.setTop(topBar);

        table = buildTable();
        root.setCenter(table);

        // Dashboard Summary (Right side)
        dashboardBox = buildDashboard();
        root.setRight(dashboardBox);

        HBox bottomBar = buildBottomBar();
        root.setBottom(bottomBar);

        // Wrap master data in FilteredList & SortedList
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Expense> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Load current month data
        LocalDate now = LocalDate.now();
        yearBox.setValue(now.getYear());
        monthBox.setValue(now.getMonth());
        refreshTable();

        Scene scene = new Scene(root, 1100, 650); // Increased width for dashboard

        // Try to load CSS if present
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ignore) {
            // No CSS, ignore
        }

        primaryStage.setTitle("Business Expense Tracker Pro");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox buildTopBar(Stage stage) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // --- Row 1: Filters ---
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Month/Year Filter
        HBox myGroup = new HBox(5, new Label("Period:"), monthBox = new ComboBox<>(), yearBox = new ComboBox<>());
        myGroup.setAlignment(Pos.CENTER_LEFT);
        monthBox.getItems().addAll(Month.values());
        monthBox.setPrefWidth(120);

        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setPrefWidth(90);

        Label orLabel = new Label("OR Range:");
        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("From Date");
        fromDatePicker.setPrefWidth(130);

        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("To Date");
        toDatePicker.setPrefWidth(130);

        Button filterBtn = new Button("Apply Filter");
        filterBtn.getStyleClass().add("primary-btn");
        filterBtn.setOnAction(e -> refreshTable());

        Button clearRangeBtn = new Button("Clear Range");
        clearRangeBtn.setOnAction(e -> {
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
        });

        filterBar.getChildren().addAll(myGroup, orLabel, fromDatePicker, toDatePicker, filterBtn, clearRangeBtn);

        // --- Row 2: Search and Actions ---
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("primary-btn");
        addBtn.setPrefWidth(90);
        addBtn.setOnAction(e -> onAdd(stage));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("primary-btn");
        editBtn.setPrefWidth(90);
        editBtn.setOnAction(e -> onEdit(stage));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-btn");
        deleteBtn.setPrefWidth(90);
        deleteBtn.setOnAction(e -> onDelete());

        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Any field...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, val) -> applySearchFilter());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button chartsBtn = new Button("Analytics");
        chartsBtn.getStyleClass().add("action-btn");
        chartsBtn.setPrefWidth(120);
        chartsBtn.setOnAction(e -> onShowCharts(stage));

        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("action-btn");
        exportBtn.setPrefWidth(120);
        exportBtn.setOnAction(e -> onExport(stage));

        actionBar.getChildren().addAll(
                addBtn, editBtn, deleteBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                searchLabel, searchField,
                actionSpacer,
                chartsBtn, exportBtn);

        container.getChildren().addAll(filterBar, actionBar);
        return container;
    }

    private VBox buildDashboard() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(10));
        box.setPrefWidth(220);
        box.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 1;");

        Label title = new Label("At a Glance");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox stats = new VBox(10);
        stats.setId("stats-container");

        box.getChildren().addAll(title, new Separator(), stats);
        return box;
    }

    private HBox buildBottomBar() {
        HBox box = new HBox();
        box.setPadding(new Insets(8, 5, 0, 5));
        box.setAlignment(Pos.CENTER_RIGHT);

        totalLabel = new Label("Total: ₹0.00");
        totalLabel.getStyleClass().add("total-label");

        box.getChildren().add(totalLabel);
        return box;
    }

    private TableView<Expense> buildTable() {
        TableView<Expense> tv = new TableView<>();

        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catCol.setPrefWidth(130);

        TableColumn<Expense, String> projCol = new TableColumn<>("Project");
        projCol.setCellValueFactory(new PropertyValueFactory<>("project"));
        projCol.setPrefWidth(130);

        TableColumn<Expense, String> payCol = new TableColumn<>("Payment");
        payCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        payCol.setPrefWidth(110);

        TableColumn<Expense, Boolean> statusCol = new TableColumn<>("Reimbursed");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("reimbursed"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value ? "YES" : "NO");
                    setStyle(value ? "-fx-text-fill: #10b981; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(110);
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(value));
                }
            }
        });

        TableColumn<Expense, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(250);

        tv.getColumns().addAll(dateCol, catCol, projCol, payCol, statusCol, amountCol, noteCol);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tv;
    }

    private void refreshTable() {
        List<Expense> list;
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from != null && to != null) {
            list = ExpenseDAO.getExpensesByRange(from, to);
        } else {
            Integer year = yearBox.getValue();
            Month month = monthBox.getValue();
            if (year == null || month == null)
                return;
            list = ExpenseDAO.getExpensesByMonthYear(year, month.getValue());
        }

        masterData.setAll(list);
        applySearchFilter(); // reapply search filter
        updateDashboard();
    }

    private void applySearchFilter() {
        String text = searchField == null ? "" : searchField.getText();
        if (filteredData == null)
            return;

        String lower = text == null ? "" : text.toLowerCase();

        Predicate<Expense> predicate;
        if (lower.isBlank()) {
            predicate = e -> true;
        } else {
            predicate = e -> (e.getCategoryName() != null && e.getCategoryName().toLowerCase().contains(lower))
                    || (e.getProject() != null && e.getProject().toLowerCase().contains(lower))
                    || (e.getPaymentMethod() != null && e.getPaymentMethod().toLowerCase().contains(lower))
                    || (e.getNote() != null && e.getNote().toLowerCase().contains(lower));
        }
        filteredData.setPredicate(predicate);
        updateDashboard();
    }

    private void updateDashboard() {
        updateTotalLabel();
        if (dashboardBox == null)
            return;
        VBox stats = (VBox) dashboardBox.lookup("#stats-container");
        if (stats == null)
            return;

        stats.getChildren().clear();

        List<Expense> current = filteredData == null ? masterData : filteredData;
        if (current.isEmpty()) {
            stats.getChildren().add(new Label("No data to show."));
            return;
        }

        // Top Categories
        Map<String, Double> catTotals = new java.util.HashMap<>();
        for (Expense e : current) {
            catTotals.merge(e.getCategoryName(), e.getAmount(), Double::sum);
        }

        Label catTitle = new Label("Top Categories:");
        catTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        stats.getChildren().add(catTitle);

        catTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    Label l = new Label(entry.getKey() + ": " + currencyFormat.format(entry.getValue()));
                    l.setStyle("-fx-font-size: 13px;");
                    stats.getChildren().add(l);
                });

        stats.getChildren().add(new Separator());

        // Top Projects
        Map<String, Double> projTotals = new java.util.HashMap<>();
        for (Expense e : current) {
            String p = e.getProject() == null || e.getProject().isBlank() ? "General" : e.getProject();
            projTotals.merge(p, e.getAmount(), Double::sum);
        }

        Label projTitle = new Label("Top Projects:");
        projTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        stats.getChildren().add(projTitle);

        projTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    Label l = new Label(entry.getKey() + ": " + currencyFormat.format(entry.getValue()));
                    l.setStyle("-fx-font-size: 13px;");
                    stats.getChildren().add(l);
                });
    }

    private void updateTotalLabel() {
        double sum = filteredData == null
                ? masterData.stream().mapToDouble(Expense::getAmount).sum()
                : filteredData.stream().mapToDouble(Expense::getAmount).sum();

        totalLabel.setText("Total: " + currencyFormat.format(sum));
    }

    private void onAdd(Stage owner) {
        Optional<Expense> result = ExpenseDialogs.showExpenseDialog(owner, null);
        result.ifPresent(exp -> {
            ExpenseDAO.insertExpense(exp);
            refreshTable();
        });
    }

    private void onEdit(Stage owner) {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select an expense to edit.");
            return;
        }
        Optional<Expense> result = ExpenseDialogs.showExpenseDialog(owner, selected);
        result.ifPresent(exp -> {
            ExpenseDAO.updateExpense(exp);
            refreshTable();
        });
    }

    private void onDelete() {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select an expense to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete selected expense?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                ExpenseDAO.deleteExpense(selected.getId());
                refreshTable();
            }
        });
    }

    private void onShowCharts(Stage owner) {
        List<Expense> data = filteredData == null ? masterData : filteredData;
        if (data.isEmpty()) {
            showInfo("No data to visualize.");
            return;
        }

        Map<String, Double> byCategory = new java.util.LinkedHashMap<>();
        Map<String, Double> byProject = new java.util.LinkedHashMap<>();
        Map<LocalDate, Double> byDay = new java.util.TreeMap<>();

        for (Expense e : data) {
            byCategory.merge(e.getCategoryName(), e.getAmount(), Double::sum);
            byProject.merge(e.getProject() == null || e.getProject().isBlank() ? "None" : e.getProject(), e.getAmount(),
                    Double::sum);
            byDay.merge(e.getDate(), e.getAmount(), Double::sum);
        }

        ChartUtils.showPieChart(owner, byCategory, "Expenses by Category (Filtered)");
        ChartUtils.showPieChart(owner, byProject, "Expenses by Project/Client (Filtered)");
        ChartUtils.showDailyBarChart(owner, byDay, "Daily Expenses (Filtered)");
    }

    private void onExport(Stage owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export expenses to CSV");
        fc.setInitialFileName("expenses_export.csv");
        var file = fc.showSaveDialog(owner);
        if (file == null)
            return;

        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            pw.println("Date,Category,Project,PaymentMethod,Reimbursed,Amount,Note");
            for (Expense e : table.getItems()) {
                String cleanNote = e.getNote() == null ? "" : e.getNote().replace(",", " ");
                String cleanProject = e.getProject() == null ? "" : e.getProject().replace(",", " ");
                pw.printf("%s,%s,%s,%s,%s,%.2f,%s%n",
                        e.getDate(),
                        e.getCategoryName(),
                        cleanProject,
                        e.getPaymentMethod(),
                        e.isReimbursed() ? "Yes" : "No",
                        e.getAmount(),
                        cleanNote);
            }
            showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to export CSV: " + ex.getMessage());
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        Platform.setImplicitExit(true);
        launch(args);
    }
}
