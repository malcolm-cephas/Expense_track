package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExpenseDto>>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable) {

        Page<ExpenseDto> page;
        if (start != null && end != null) {
            page = expenseService.getExpensesByDateRange(userDetails.getUsername(), start, end, pageable);
        } else {
            page = expenseService.getAllExpenses(userDetails.getUsername(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(page, "Expenses fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseDto created = expenseService.createExpense(userDetails.getUsername(), expenseDto);
        return ResponseEntity.ok(ApiResponse.success(created, "Expense created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseDto updated = expenseService.updateExpense(userDetails.getUsername(), id, expenseDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Expense updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        expenseService.deleteExpense(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted successfully"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> stats = expenseService.getAnalytics(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(stats, "Analytics fetched successfully"));
    }
}
