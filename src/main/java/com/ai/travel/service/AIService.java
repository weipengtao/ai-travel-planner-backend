package com.ai.travel.service;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {
    
    @Value("${doubao.api-key}")
    private String apiKey;
    
    @Value("${doubao.base-url}")
    private String baseUrl;
    
    @Value("${doubao.model}")
    private String model;
    
    /**
     * 调用豆包API生成旅行计划
     * @param travelRequest 用户旅行需求
     * @return AI生成的旅行计划文本
     */
    public String generateTravelPlan(String travelRequest) {
        ArkService arkService = ArkService.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // 构建系统提示词
        String systemPrompt = "你是一个专业的旅行规划师。请根据用户的旅行需求，生成一个详细、实用的旅行计划。" +
                "计划应该包括：目的地、旅行天数、每日行程安排（包括时间、景点、活动、预算等）、总预算估算。" +
                "请以JSON格式返回，包含以下字段：destination, duration, totalBudget, days（数组，包含day, date, title, activities数组）。" +
                "activities数组包含：name, time, budget, description。" +
                "请确保返回的数据结构清晰，便于前端解析。";

        List<ChatMessage> chatMessages = new ArrayList<>();
        
        // 添加系统消息
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(systemPrompt)
                .build();
        chatMessages.add(systemMessage);

        // 添加用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("请为以下旅行需求生成计划：" + travelRequest)
                .build();
        chatMessages.add(userMessage);

        // 创建聊天完成请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(chatMessages)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        try {
            // 发送请求并获取响应
            String response = arkService.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .stream()
                    .findFirst()
                    .map(choice -> choice.getMessage().getContent())
                    .orElse("").toString();
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("调用豆包API失败: " + e.getMessage(), e);
        } finally {
            arkService.shutdownExecutor();
        }
    }
    
    /**
     * 解析AI返回的JSON数据，如果解析失败则返回模拟数据
     * @param aiResponse AI返回的文本
     * @param travelRequest 原始旅行需求
     * @return 解析后的旅行计划数据
     */
    public String parseAIPlan(String aiResponse, String travelRequest) {
        try {
            // 尝试解析JSON，如果失败则返回模拟数据
            if (aiResponse.trim().startsWith("{") && aiResponse.trim().endsWith("}")) {
                return aiResponse;
            } else {
                // 如果AI返回的不是标准JSON，返回模拟数据
                return generateMockPlan(travelRequest);
            }
        } catch (Exception e) {
            return generateMockPlan(travelRequest);
        }
    }
    
    /**
     * 生成模拟旅行计划数据
     * @param travelRequest 旅行需求
     * @return 模拟的旅行计划JSON
     */
    private String generateMockPlan(String travelRequest) {
        // 根据旅行需求生成不同的模拟数据
        if (travelRequest.contains("上海") || travelRequest.toLowerCase().contains("shanghai")) {
            return """
            {
                "destination": "上海",
                "duration": 3,
                "totalBudget": 2800,
                "days": [
                    {
                        "day": 1,
                        "date": "2024-01-15",
                        "title": "外滩与陆家嘴",
                        "activities": [
                            {
                                "name": "外滩",
                                "time": "09:00-11:00",
                                "budget": 0,
                                "description": "欣赏万国建筑博览群，感受上海的历史韵味"
                            },
                            {
                                "name": "东方明珠",
                                "time": "11:30-13:30",
                                "budget": 120,
                                "description": "登塔俯瞰上海全景，体验城市地标"
                            },
                            {
                                "name": "南京路步行街",
                                "time": "14:00-17:00",
                                "budget": 200,
                                "description": "购物与品尝美食，体验繁华商业街"
                            }
                        ]
                    },
                    {
                        "day": 2,
                        "date": "2024-01-16",
                        "title": "迪士尼乐园",
                        "activities": [
                            {
                                "name": "上海迪士尼",
                                "time": "09:00-21:00",
                                "budget": 399,
                                "description": "全天游玩迪士尼主题乐园，体验童话世界"
                            }
                        ]
                    },
                    {
                        "day": 3,
                        "date": "2024-01-17",
                        "title": "文化探索",
                        "activities": [
                            {
                                "name": "豫园",
                                "time": "09:00-11:30",
                                "budget": 40,
                                "description": "游览古典园林，品尝城隍庙小吃"
                            },
                            {
                                "name": "田子坊",
                                "time": "14:00-17:00",
                                "budget": 150,
                                "description": "探索文艺小巷，感受创意文化"
                            }
                        ]
                    }
                ]
            }
            """;
        } else {
            // 默认北京计划
            return """
            {
                "destination": "北京",
                "duration": 3,
                "totalBudget": 2500,
                "days": [
                    {
                        "day": 1,
                        "date": "2024-01-15",
                        "title": "故宫与天安门",
                        "activities": [
                            {
                                "name": "天安门广场",
                                "time": "09:00-10:30",
                                "budget": 0,
                                "description": "观看升旗仪式，游览广场"
                            },
                            {
                                "name": "故宫博物院",
                                "time": "10:30-16:00",
                                "budget": 60,
                                "description": "参观明清皇宫建筑群"
                            },
                            {
                                "name": "王府井大街",
                                "time": "18:00-20:00",
                                "budget": 150,
                                "description": "品尝北京小吃，购物"
                            }
                        ]
                    },
                    {
                        "day": 2,
                        "date": "2024-01-16",
                        "title": "长城一日游",
                        "activities": [
                            {
                                "name": "八达岭长城",
                                "time": "08:00-15:00",
                                "budget": 45,
                                "description": "攀登长城，欣赏壮丽景色"
                            },
                            {
                                "name": "明十三陵",
                                "time": "15:30-17:30",
                                "budget": 30,
                                "description": "参观明代皇家陵墓"
                            }
                        ]
                    },
                    {
                        "day": 3,
                        "date": "2024-01-17",
                        "title": "胡同文化与颐和园",
                        "activities": [
                            {
                                "name": "什刹海胡同",
                                "time": "09:00-11:30",
                                "budget": 80,
                                "description": "乘坐三轮车游览老北京胡同"
                            },
                            {
                                "name": "颐和园",
                                "time": "13:00-17:00",
                                "budget": 30,
                                "description": "游览皇家园林，昆明湖泛舟"
                            }
                        ]
                    }
                ]
            }
            """;
        }
    }
}