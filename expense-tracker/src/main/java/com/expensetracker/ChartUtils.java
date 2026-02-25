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

    public static void showPieChart(Window owner,
            Map<String, Double> data,
            String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false);

        showInSwingWindow(owner, chart, title);
    }

    public static void showDailyBarChart(Window owner,
            Map<LocalDate, Double> data,
            String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((date, total) -> dataset.addValue(total, "Expense", date.toString()));

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Date",
                "Amount",
                dataset);

        showInSwingWindow(owner, chart, title);
    }

    private static void showInSwingWindow(Window owner,
            JFreeChart chart,
            String title) {
        SwingNode swingNode = new SwingNode();

        // Create the Swing component on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            ChartPanel panel = new ChartPanel(chart);
            // Set a preferred size to help SwingNode calculate layout immediately
            panel.setPreferredSize(new java.awt.Dimension(800, 600));
            // Optional: Enable mouse zooming/panning
            panel.setMouseWheelEnabled(true);

            // Set the content on the JavaFX Application Thread
            Platform.runLater(() -> swingNode.setContent(panel));
        });

        BorderPane root = new BorderPane(swingNode);
        // Match the scene size to the panel's preferred size
        Scene scene = new Scene(root, 800, 600);

        Stage stage = new Stage();
        if (owner != null)
            stage.initOwner(owner);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
