package com.ai.travel.service;

import com.ai.travel.entity.TravelPlan;
import com.ai.travel.entity.User;
import com.ai.travel.repository.TravelPlanRepository;
import com.ai.travel.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建新的旅行计划
     */
    public TravelPlan createTravelPlan(User user, String travelRequest) {
        // 调用AI服务生成旅行计划
        String aiResponse = aiService.generateTravelPlan(travelRequest);
        String planData = aiService.parseAIPlan(aiResponse, travelRequest);

        // 解析计划数据
        JsonNode planJson = parseJson(planData);

        // 提取字段
        String destination = extractDestination(planJson);
        Integer duration = extractDuration(planJson);
        BigDecimal totalBudget = extractTotalBudget(planJson);

        // 创建旅行计划实体
        TravelPlan travelPlan = new TravelPlan(
                user,
                destination,
                duration,
                totalBudget,
                travelRequest,
                planData
        );

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
     * 检查是否存在完全相同的旅行计划（避免重复生成）
     */
    public boolean hasExactSamePlan(User user, String travelRequest) {
        List<TravelPlan> exactPlans = travelPlanRepository.findByUserAndTravelRequestOrderByCreatedAtDesc(user, travelRequest);
        return !exactPlans.isEmpty();
    }
    
    /**
     * 检查是否存在相似的旅行计划（避免重复生成）
     */
    public boolean hasSimilarPlan(User user, String travelRequest) {
        // 首先检查是否有完全相同的计划
        if (hasExactSamePlan(user, travelRequest)) {
            return true;
        }
        
        // 如果没有完全相同的，再检查是否有相似的
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
        travelPlanRepository.findByIdAndUser(id, user)
                .ifPresent(travelPlanRepository::delete);
    }

    /**
     * 使用 Jackson 安全解析 JSON
     */
    private JsonNode parseJson(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            return objectMapper.createObjectNode(); // 返回空 JSON 节点
        }
    }

    /**
     * 从 JSON 提取目的地
     */
    private String extractDestination(JsonNode jsonNode) {
        JsonNode destNode = jsonNode.path("destination");
        if (!destNode.isMissingNode() && !destNode.asText().isEmpty()) {
            return destNode.asText();
        }
        return "未知目的地";
    }

    /**
     * 从 JSON 提取行程天数
     */
    private Integer extractDuration(JsonNode jsonNode) {
        JsonNode durationNode = jsonNode.path("duration");
        if (durationNode.isInt()) {
            return durationNode.asInt();
        }
        return 3; // 默认3天
    }

    /**
     * 从 JSON 提取总预算
     */
    private BigDecimal extractTotalBudget(JsonNode jsonNode) {
        // 1. 优先使用显式 totalBudget 字段
        JsonNode budgetNode = jsonNode.path("totalBudget");
        if (budgetNode.isNumber()) {
            BigDecimal value = budgetNode.decimalValue();
            if (value.compareTo(BigDecimal.ZERO) > 0) {
                return value;
            }
        }

        // 2. 若未提供 totalBudget，则根据 days[].activities[].budget 汇总
        BigDecimal total = BigDecimal.ZERO;
        JsonNode days = jsonNode.path("days");
        if (days.isArray()) {
            for (JsonNode day : days) {
                JsonNode activities = day.path("activities");
                if (activities.isArray()) {
                    for (JsonNode activity : activities) {
                        JsonNode budget = activity.path("budget");
                        if (budget.isNumber()) {
                            total = total.add(budget.decimalValue());
                        }
                    }
                }
            }
        }

        // 如果总和仍为 0，则设定一个合理默认值
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            total = BigDecimal.valueOf(3000);
        }

        return total;
    }

    /**
     * 从旅行需求中提取关键词
     */
    private String[] extractKeywords(String travelRequest) {
        return travelRequest.toLowerCase()
                .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ")
                .split("\\s+");
    }
}
