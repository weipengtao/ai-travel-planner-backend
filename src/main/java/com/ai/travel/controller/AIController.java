package com.ai.travel.controller;

import com.ai.travel.entity.Expense;
import com.ai.travel.entity.TravelPlan;
import com.ai.travel.entity.User;
import com.ai.travel.security.JwtUtils;
import com.ai.travel.service.AIService;
import com.ai.travel.service.ExpenseService;
import com.ai.travel.service.TravelPlanService;
import com.ai.travel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 生成旅行计划并保存到数据库
     * @param request 包含旅行需求的请求体
     * @param authorization JWT token
     * @return 旅行计划数据
     */
    @PostMapping("/plan")
    public ResponseEntity<?> generateTravelPlan(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            String travelRequest = request.get("travelRequest");
            String forceRegenerate = request.get("forceRegenerate");
            boolean shouldForceRegenerate = "true".equals(forceRegenerate);
            
            if (travelRequest == null || travelRequest.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("旅行需求不能为空"));
            }
            
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            // 如果不是强制重新生成，检查是否存在完全相同的旅行计划
            if (!shouldForceRegenerate && travelPlanService.hasExactSamePlan(user, travelRequest)) {
                // 如果存在完全相同的计划，返回最近的一个
                Optional<TravelPlan> latestPlan = travelPlanService.getLatestTravelPlan(user);
                if (latestPlan.isPresent()) {
                    String planData = latestPlan.get().getPlanData();
                    return ResponseEntity.ok(createSuccessResponse(planData, "使用已有的相同旅行计划"));
                }
            }
            
            // 创建新的旅行计划
            TravelPlan travelPlan = travelPlanService.createTravelPlan(user, travelRequest);
            
            return ResponseEntity.ok(createSuccessResponse(travelPlan.getPlanData(), "旅行计划生成成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("生成旅行计划失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户最近创建的旅行计划
     * @param authorization JWT token
     * @return 最近创建的旅行计划
     */
    @GetMapping("/plan/latest")
    public ResponseEntity<?> getLatestTravelPlan(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            Optional<TravelPlan> latestPlan = travelPlanService.getLatestTravelPlan(user);
            if (latestPlan.isPresent()) {
                // 创建包含计划数据和原始旅行需求的响应
                Map<String, Object> planInfo = new HashMap<>();
                planInfo.put("planData", latestPlan.get().getPlanData());
                planInfo.put("travelRequest", latestPlan.get().getTravelRequest());
                
                return ResponseEntity.ok(createSuccessResponse(planInfo, "获取最近旅行计划成功"));
            } else {
                return ResponseEntity.ok(createSuccessResponse(null, "暂无旅行计划"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取旅行计划失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户所有旅行计划
     * @param authorization JWT token
     * @return 用户的所有旅行计划
     */
    @GetMapping("/plan/all")
    public ResponseEntity<?> getAllTravelPlans(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            // 获取用户所有旅行计划（简化返回，只返回基本信息）
            var plans = travelPlanService.getUserTravelPlans(user).stream()
                    .map(plan -> {
                        Map<String, Object> planInfo = new HashMap<>();
                        planInfo.put("id", plan.getId());
                        planInfo.put("destination", plan.getDestination());
                        planInfo.put("duration", plan.getDuration());
                        planInfo.put("travelRequest", plan.getTravelRequest());
                        planInfo.put("createdAt", plan.getCreatedAt());
                        return planInfo;
                    })
                    .toList();
            
            return ResponseEntity.ok(createSuccessResponse(plans, "获取旅行计划列表成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取旅行计划列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取特定旅行计划的详细信息
     * @param planId 旅行计划ID
     * @param authorization JWT token
     * @return 旅行计划的详细信息
     */
    @GetMapping("/plan/{planId}")
    public ResponseEntity<?> getTravelPlanById(
            @PathVariable Long planId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            Optional<TravelPlan> travelPlan = travelPlanService.getTravelPlanById(planId, user);
            if (travelPlan.isPresent()) {
                Map<String, Object> planDetails = new HashMap<>();
                planDetails.put("id", travelPlan.get().getId());
                planDetails.put("destination", travelPlan.get().getDestination());
                planDetails.put("duration", travelPlan.get().getDuration());
                planDetails.put("totalBudget", travelPlan.get().getTotalBudget());
                planDetails.put("travelRequest", travelPlan.get().getTravelRequest());
                planDetails.put("planData", travelPlan.get().getPlanData());
                planDetails.put("createdAt", travelPlan.get().getCreatedAt());
                planDetails.put("updatedAt", travelPlan.get().getUpdatedAt());
                
                return ResponseEntity.ok(createSuccessResponse(planDetails, "获取旅行计划详情成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("旅行计划不存在或无权访问"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取旅行计划详情失败: " + e.getMessage()));
        }
    }

    /**
     * 添加消费记录
     */
    @PostMapping("/expense")
    public ResponseEntity<?> addExpense(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            Long travelPlanId = Long.valueOf(request.get("travelPlanId").toString());
            Integer dayNumber = Integer.valueOf(request.get("dayNumber").toString());
            String item = request.get("item").toString();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String category = request.get("category") != null ? request.get("category").toString() : "其他";
            
            Expense expense = expenseService.addExpense(user, travelPlanId, dayNumber, item, amount, category);
            
            return ResponseEntity.ok(createSuccessResponse(expense, "消费记录添加成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("添加消费记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除消费记录
     */
    @DeleteMapping("/expense/{expenseId}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable Long expenseId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            expenseService.deleteExpense(user, expenseId);
            
            return ResponseEntity.ok(createSuccessResponse(null, "消费记录删除成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("删除消费记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取旅行计划某天的消费记录
     */
    @GetMapping("/expense/{travelPlanId}/{dayNumber}")
    public ResponseEntity<?> getExpensesByDay(
            @PathVariable Long travelPlanId,
            @PathVariable Integer dayNumber,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            List<Expense> expenses = expenseService.getExpensesByTravelPlanAndDay(user, travelPlanId, dayNumber);
            
            return ResponseEntity.ok(createSuccessResponse(expenses, "获取消费记录成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取消费记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取旅行计划的总消费
     */
    @GetMapping("/expense/total/{travelPlanId}")
    public ResponseEntity<?> getTotalExpense(
            @PathVariable Long travelPlanId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 验证用户身份
            User user = validateUser(authorization);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户未登录或token无效"));
            }
            
            BigDecimal totalExpense = expenseService.getTotalExpenseByTravelPlan(user, travelPlanId);
            
            return ResponseEntity.ok(createSuccessResponse(totalExpense, "获取总消费成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取总消费失败: " + e.getMessage()));
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AI Travel Planner");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证用户身份
     */
    private User validateUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authorization.substring(7);
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        if (username != null && jwtUtils.validateJwtToken(token)) {
            return userService.findByUsername(username);
        }
        
        return null;
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}