package com.ai.travel.controller;

import com.ai.travel.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * 生成旅行计划
     * @param request 包含旅行需求的请求体
     * @return 旅行计划数据
     */
    @PostMapping("/plan")
    public ResponseEntity<?> generateTravelPlan(@RequestBody Map<String, String> request) {
        try {
            String travelRequest = request.get("travelRequest");
            
            if (travelRequest == null || travelRequest.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("旅行需求不能为空"));
            }

            // 调用AI服务生成旅行计划
            String aiResponse = aiService.generateTravelPlan(travelRequest);
            
            // 解析AI返回的数据
            String planData = aiService.parseAIPlan(aiResponse, travelRequest);
            
            return ResponseEntity.ok(createSuccessResponse(planData, "旅行计划生成成功"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("生成旅行计划失败: " + e.getMessage()));
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