package com.ai.travel.repository;

import com.ai.travel.entity.Expense;
import com.ai.travel.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    /**
     * 根据旅行计划ID查找消费记录
     */
    List<Expense> findByTravelPlanOrderByCreatedAtDesc(TravelPlan travelPlan);
    
    /**
     * 根据旅行计划ID和天数查找消费记录
     */
    List<Expense> findByTravelPlanAndDayNumberOrderByCreatedAtDesc(TravelPlan travelPlan, Integer dayNumber);
    
    /**
     * 根据旅行计划ID统计总消费金额
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.travelPlan = :travelPlan")
    BigDecimal sumAmountByTravelPlan(@Param("travelPlan") TravelPlan travelPlan);
    
    /**
     * 根据旅行计划ID和天数统计消费金额
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.travelPlan = :travelPlan AND e.dayNumber = :dayNumber")
    BigDecimal sumAmountByTravelPlanAndDay(@Param("travelPlan") TravelPlan travelPlan, @Param("dayNumber") Integer dayNumber);
    
    /**
     * 根据旅行计划ID删除消费记录
     */
    void deleteByTravelPlan(TravelPlan travelPlan);
    
    /**
     * 根据旅行计划ID和天数删除消费记录
     */
    void deleteByTravelPlanAndDayNumber(TravelPlan travelPlan, Integer dayNumber);
}