package com.expensetracker;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.time.LocalDate;
import java.util.Map;

public class ChartUtils {

    public static void showCategoryPieChart(Window owner,
                                            Map<String, Double> data,
                                            String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false
        );

        showInSwingWindow(owner, chart, title);
    }

    public static void showDailyBarChart(Window owner,
                                         Map<LocalDate, Double> data,
                                         String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((date, total) ->
                dataset.addValue(total, "Expense", date.toString())
        );

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Date",
                "Amount",
                dataset
        );

        showInSwingWindow(owner, chart, title);
    }

    private static void showInSwingWindow(Window owner,
                                          JFreeChart chart,
                                          String title) {
        SwingNode swingNode = new SwingNode();
        Platform.runLater(() -> {
            ChartPanel panel = new ChartPanel(chart);
            swingNode.setContent(panel);
        });

        BorderPane root = new BorderPane(swingNode);
        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        if (owner != null) stage.initOwner(owner);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
