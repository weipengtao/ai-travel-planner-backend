package com.ai.travel.service;

import com.ai.travel.entity.TravelPlan;
import com.ai.travel.entity.User;
import com.ai.travel.repository.TravelPlanRepository;
import com.ai.travel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelPlanService {
    
    @Autowired
    private TravelPlanRepository travelPlanRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AIService aiService;
    
    /**
     * 创建新的旅行计划
     */
    public TravelPlan createTravelPlan(User user, String travelRequest) {
        // 调用AI服务生成旅行计划
        String aiResponse = aiService.generateTravelPlan(travelRequest);
        String planData = aiService.parseAIPlan(aiResponse, travelRequest);
        
        // 解析计划数据获取目的地和天数
        String destination = extractDestinationFromPlanData(planData);
        Integer duration = extractDurationFromPlanData(planData);
        
        // 创建旅行计划实体
        TravelPlan travelPlan = new TravelPlan(user, destination, duration, null, travelRequest, planData);
        
        return travelPlanRepository.save(travelPlan);
    }
    
    /**
     * 根据用户ID获取所有旅行计划
     */
    public List<TravelPlan> getUserTravelPlans(User user) {
        return travelPlanRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 根据ID获取旅行计划（确保属于当前用户）
     */
    public Optional<TravelPlan> getTravelPlanById(Long id, User user) {
        return travelPlanRepository.findByIdAndUser(id, user);
    }
    
    /**
     * 获取用户最近创建的旅行计划
     */
    public Optional<TravelPlan> getLatestTravelPlan(User user) {
        return travelPlanRepository.findFirstByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * 检查是否存在相似的旅行计划（避免重复生成）
     */
    public boolean hasSimilarPlan(User user, String travelRequest) {
        // 提取关键词进行相似性检查
        String[] keywords = extractKeywords(travelRequest);
        for (String keyword : keywords) {
            List<TravelPlan> similarPlans = travelPlanRepository.findSimilarPlansByUserAndKeyword(user, keyword);
            if (!similarPlans.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 删除旅行计划
     */
    public void deleteTravelPlan(Long id, User user) {
        Optional<TravelPlan> travelPlan = travelPlanRepository.findByIdAndUser(id, user);
        travelPlan.ifPresent(travelPlanRepository::delete);
    }
    
    /**
     * 从计划数据中提取目的地
     */
    private String extractDestinationFromPlanData(String planData) {
        try {
            // 简单的JSON解析提取目的地
            if (planData.contains("\"destination\":")) {
                int start = planData.indexOf("\"destination\":") + 14;
                int end = planData.indexOf("\",", start);
                if (end > start) {
                    return planData.substring(start, end);
                }
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
        }
        return "未知目的地";
    }
    
    /**
     * 从计划数据中提取天数
     */
    private Integer extractDurationFromPlanData(String planData) {
        try {
            // 简单的JSON解析提取天数
            if (planData.contains("\"duration\":")) {
                int start = planData.indexOf("\"duration\":") + 11;
                int end = planData.indexOf(",", start);
                if (end > start) {
                    String durationStr = planData.substring(start, end).trim();
                    return Integer.parseInt(durationStr);
                }
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
        }
        return 3; // 默认3天
    }
    
    /**
     * 从旅行需求中提取关键词
     */
    private String[] extractKeywords(String travelRequest) {
        // 简单的关键词提取逻辑
        return travelRequest.toLowerCase()
                .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ")
                .split("\\s+");
    }
}