package com.ai.travel.service;

import com.ai.travel.entity.User;
import com.ai.travel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }
    
    /**
     * 根据邮箱查找用户
     */
    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }
    
    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 保存用户
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}