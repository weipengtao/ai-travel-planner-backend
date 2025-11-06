package com.ai.travel.repository;

import com.ai.travel.entity.TravelPlan;
import com.ai.travel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    
    /**
     * 根据用户ID查找旅行计划
     */
    List<TravelPlan> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 根据用户ID和目的地查找旅行计划
     */
    List<TravelPlan> findByUserAndDestinationContainingIgnoreCaseOrderByCreatedAtDesc(User user, String destination);
    
    /**
     * 根据用户ID和旅行计划ID查找旅行计划
     */
    Optional<TravelPlan> findByIdAndUser(Long id, User user);
    
    /**
     * 根据用户ID和旅行需求查找完全相同的旅行计划
     */
    List<TravelPlan> findByUserAndTravelRequestOrderByCreatedAtDesc(User user, String travelRequest);
    
    /**
     * 统计用户创建的旅行计划数量
     */
    long countByUser(User user);
    
    /**
     * 根据旅行需求内容查找相似的旅行计划（用于避免重复生成）
     */
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user = :user AND tp.travelRequest LIKE %:keyword% ORDER BY tp.createdAt DESC")
    List<TravelPlan> findSimilarPlansByUserAndKeyword(@Param("user") User user, @Param("keyword") String keyword);
    
    /**
     * 查找用户最近创建的旅行计划
     */
    Optional<TravelPlan> findFirstByUserOrderByCreatedAtDesc(User user);
}