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
    private TextField searchField;
    private Label totalLabel;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    public void start(Stage primaryStage) {
        Database.init(); // ensure DB + tables + categories

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBar = buildTopBar(primaryStage);
        root.setTop(topBar);

        table = buildTable();
        root.setCenter(table);

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

        Scene scene = new Scene(root, 950, 550);

        // Try to load CSS if present
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );
        } catch (Exception ignore) {
            // No CSS, ignore
        }

        primaryStage.setTitle("Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox buildTopBar(Stage stage) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 5, 10, 5));

        Label monthLabel = new Label("Month:");
        monthBox = new ComboBox<>();
        monthBox.getItems().addAll(Month.values());

        Label yearLabel = new Label("Year:");
        yearBox = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            yearBox.getItems().add(y);
        }

        Button filterBtn = new Button("Apply Filter");
        filterBtn.setOnAction(e -> refreshTable());

        // Search field – filters by category or note
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Category or note...");
        searchField.textProperty().addListener((obs, old, val) -> applySearchFilter());

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> onAdd(stage));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> onEdit(stage));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> onDelete());

        Button chartsBtn = new Button("Show Charts");
        chartsBtn.setOnAction(e -> onShowCharts(stage));

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> onExport(stage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(
                monthLabel, monthBox,
                yearLabel, yearBox,
                filterBtn,
                searchLabel, searchField,
                spacer,
                addBtn, editBtn, deleteBtn,
                chartsBtn, exportBtn
        );
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
        dateCol.setPrefWidth(120);

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catCol.setPrefWidth(160);

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(120);
        // Format as currency in the cell
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
        noteCol.setPrefWidth(420);

        tv.getColumns().addAll(dateCol, catCol, amountCol, noteCol);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tv;
    }

    private void refreshTable() {
        Integer year = yearBox.getValue();
        Month month = monthBox.getValue();
        if (year == null || month == null) return;

        List<Expense> list = ExpenseDAO.getExpensesByMonthYear(year, month.getValue());
        masterData.setAll(list);
        applySearchFilter(); // reapply search filter
        updateTotalLabel();
    }

    private void applySearchFilter() {
        String text = searchField == null ? "" : searchField.getText();
        if (filteredData == null) return;

        String lower = text == null ? "" : text.toLowerCase();

        Predicate<Expense> predicate;
        if (lower.isBlank()) {
            predicate = e -> true;
        } else {
            predicate = e ->
                    (e.getCategoryName() != null &&
                            e.getCategoryName().toLowerCase().contains(lower))
                    || (e.getNote() != null &&
                            e.getNote().toLowerCase().contains(lower));
        }
        filteredData.setPredicate(predicate);
        updateTotalLabel();
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
        Integer year = yearBox.getValue();
        Month month = monthBox.getValue();
        if (year == null || month == null) return;

        Map<String, Double> byCategory =
                ExpenseDAO.getMonthlyTotalsByCategory(year, month.getValue());
        Map<LocalDate, Double> byDay =
                ExpenseDAO.getDailyTotals(year, month.getValue());

        ChartUtils.showCategoryPieChart(owner, byCategory,
                "Expenses by Category - " + month + " " + year);
        ChartUtils.showDailyBarChart(owner, byDay,
                "Daily Expenses - " + month + " " + year);
    }

    private void onExport(Stage owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export expenses to CSV");
        fc.setInitialFileName("expenses_" + monthBox.getValue() + "_" + yearBox.getValue() + ".csv");
        var file = fc.showSaveDialog(owner);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            pw.println("Date,Category,Amount,Note");
            for (Expense e : table.getItems()) {
                String cleanNote = e.getNote() == null ? "" : e.getNote().replace(",", " ");
                pw.printf("%s,%s,%.2f,%s%n",
                        e.getDate(),
                        e.getCategoryName(),
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
