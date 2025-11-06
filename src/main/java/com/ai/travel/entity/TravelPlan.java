package com.ai.travel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_plans")
public class TravelPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "destination", nullable = false, length = 100)
    private String destination;
    
    @Column(name = "duration", nullable = false)
    private Integer duration;
    
    @Column(name = "total_budget", precision = 10, scale = 2)
    private BigDecimal totalBudget;
    
    @Column(name = "travel_request", columnDefinition = "TEXT")
    private String travelRequest;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan_data", columnDefinition = "JSON")
    private String planData;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public TravelPlan() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TravelPlan(User user, String destination, Integer duration, 
                     BigDecimal totalBudget, String travelRequest, String planData) {
        this();
        this.user = user;
        this.destination = destination;
        this.duration = duration;
        this.totalBudget = totalBudget;
        this.travelRequest = travelRequest;
        this.planData = planData;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public BigDecimal getTotalBudget() {
        return totalBudget;
    }
    
    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }
    
    public String getTravelRequest() {
        return travelRequest;
    }
    
    public void setTravelRequest(String travelRequest) {
        this.travelRequest = travelRequest;
    }
    
    public String getPlanData() {
        return planData;
    }
    
    public void setPlanData(String planData) {
        this.planData = planData;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}