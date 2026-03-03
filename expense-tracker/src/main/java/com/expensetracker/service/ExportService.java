package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public String exportToCsv(String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Expense> expenses = expenseRepository.findByUserOrderByDateDesc(user);

        StringWriter writer = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            String[] header = { "Date", "Category", "Amount", "Project", "Payment Method", "Reimbursed", "Note" };
            csvWriter.writeNext(header);

            for (Expense expense : expenses) {
                String[] data = {
                        expense.getDate().toString(),
                        expense.getCategory().getName(),
                        expense.getAmount().toString(),
                        expense.getProject() != null ? expense.getProject() : "",
                        expense.getPaymentMethod() != null ? expense.getPaymentMethod() : "",
                        expense.isReimbursed() ? "Yes" : "No",
                        expense.getNote() != null ? expense.getNote() : ""
                };
                csvWriter.writeNext(data);
            }
        }
        return writer.toString();
    }

    public ByteArrayInputStream exportToPdf(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Expense> expenses = expenseRepository.findByUserOrderByDateDesc(user);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Title
            Paragraph title = new Paragraph("Expense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("User: " + username, headerFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            document.add(Chunk.NEWLINE);

            // Table with 5 columns
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100f);
            table.setWidths(new float[] { 2.5f, 3.5f, 2.5f, 3f, 4.5f });

            // Headers
            String[] headers = { "Date", "Category", "Amount", "Method", "Note" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            for (Expense expense : expenses) {
                table.addCell(new PdfPCell(new Phrase(expense.getDate().toString(), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(expense.getCategory().getName(), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(expense.getAmount().toString(), bodyFont)));
                table.addCell(new PdfPCell(
                        new Phrase(expense.getPaymentMethod() != null ? expense.getPaymentMethod() : "-", bodyFont)));
                table.addCell(new PdfPCell(new Phrase(expense.getNote() != null ? expense.getNote() : "", bodyFont)));
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
