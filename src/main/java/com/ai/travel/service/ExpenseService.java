package com.ai.travel.service;

import com.ai.travel.entity.Expense;
import com.ai.travel.entity.TravelPlan;
import com.ai.travel.entity.User;
import com.ai.travel.repository.ExpenseRepository;
import com.ai.travel.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private TravelPlanRepository travelPlanRepository;
    
    /**
     * 添加消费记录
     */
    public Expense addExpense(User user, Long travelPlanId, Integer dayNumber, 
                             String item, BigDecimal amount, String category) {
        // 验证旅行计划属于当前用户
        Optional<TravelPlan> travelPlan = travelPlanRepository.findByIdAndUser(travelPlanId, user);
        if (travelPlan.isEmpty()) {
            throw new RuntimeException("旅行计划不存在或无权访问");
        }
        
        Expense expense = new Expense(travelPlan.get(), dayNumber, item, amount, category);
        return expenseRepository.save(expense);
    }
    
    /**
     * 删除消费记录
     */
    public void deleteExpense(User user, Long expenseId) {
        // 验证消费记录属于当前用户
        Optional<Expense> expense = expenseRepository.findById(expenseId);
        if (expense.isPresent() && expense.get().getTravelPlan().getUser().getId().equals(user.getId())) {
            expenseRepository.delete(expense.get());
        } else {
            throw new RuntimeException("消费记录不存在或无权删除");
        }
    }
    
    /**
     * 获取旅行计划某天的消费记录
     */
    public List<Expense> getExpensesByTravelPlanAndDay(User user, Long travelPlanId, Integer dayNumber) {
        // 验证旅行计划属于当前用户
        Optional<TravelPlan> travelPlan = travelPlanRepository.findByIdAndUser(travelPlanId, user);
        if (travelPlan.isEmpty()) {
            throw new RuntimeException("旅行计划不存在或无权访问");
        }
        
        return expenseRepository.findByTravelPlanAndDayNumberOrderByCreatedAtDesc(travelPlan.get(), dayNumber);
    }
    
    /**
     * 获取旅行计划的所有消费记录
     */
    public List<Expense> getExpensesByTravelPlan(User user, Long travelPlanId) {
        // 验证旅行计划属于当前用户
        Optional<TravelPlan> travelPlan = travelPlanRepository.findByIdAndUser(travelPlanId, user);
        if (travelPlan.isEmpty()) {
            throw new RuntimeException("旅行计划不存在或无权访问");
        }
        
        return expenseRepository.findByTravelPlanOrderByCreatedAtDesc(travelPlan.get());
    }
    
    /**
     * 计算某天的总消费
     */
    public BigDecimal getTotalExpenseByDay(User user, Long travelPlanId, Integer dayNumber) {
        List<Expense> expenses = getExpensesByTravelPlanAndDay(user, travelPlanId, dayNumber);
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 计算旅行计划的总消费
     */
    public BigDecimal getTotalExpenseByTravelPlan(User user, Long travelPlanId) {
        List<Expense> expenses = getExpensesByTravelPlan(user, travelPlanId);
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}