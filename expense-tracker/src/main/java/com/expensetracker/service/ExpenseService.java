package com.expensetracker.service;

import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<ExpenseDto> getAllExpenses(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return expenseRepository.findByUser(user, pageable).map(this::mapToDto);
    }

    public Page<ExpenseDto> getExpensesByDateRange(String username, LocalDate start, LocalDate end, Pageable pageable) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return expenseRepository.findByUserAndDateBetween(user, start, end, pageable).map(this::mapToDto);
    }

    public ExpenseDto createExpense(String username, ExpenseDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Expense expense = Expense.builder()
                .amount(dto.getAmount())
                .date(dto.getDate())
                .category(category)
                .user(user)
                .project(dto.getProject())
                .paymentMethod(dto.getPaymentMethod())
                .reimbursed(dto.isReimbursed())
                .note(dto.getNote())
                .build();

        return mapToDto(expenseRepository.save(expense));
    }

    public ExpenseDto updateExpense(String username, Long id, ExpenseDto dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to update this expense");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setCategory(category);
        expense.setProject(dto.getProject());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setReimbursed(dto.isReimbursed());
        expense.setNote(dto.getNote());

        return mapToDto(expenseRepository.save(expense));
    }

    public void deleteExpense(String username, Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }

    public Map<String, Object> getAnalytics(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        BigDecimal total = expenseRepository.getTotalExpenseByUser(user);
        List<Map<String, Object>> categoryWise = expenseRepository.getCategoryWiseTotal(user);
        List<Map<String, Object>> monthlyTrend = expenseRepository.getMonthlyTrend(user);

        return Map.of(
                "totalExpense", total != null ? total : BigDecimal.ZERO,
                "categoryUsage", categoryWise,
                "monthlyTrend", monthlyTrend);
    }

    private ExpenseDto mapToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        dto.setCategoryId(expense.getCategory().getId());
        dto.setCategoryName(expense.getCategory().getName());
        dto.setProject(expense.getProject());
        dto.setPaymentMethod(expense.getPaymentMethod());
        dto.setReimbursed(expense.isReimbursed());
        dto.setNote(expense.getNote());
        return dto;
    }
}
