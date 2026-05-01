package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemAuthService;
import com.pd.modules.system.security.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SystemAuthServiceImpl implements SystemAuthService {

    @Override
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> data = new HashMap<>();
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof LoginUser loginUser) {
                data.put("username", loginUser.getUsername());
                data.put("userId", loginUser.getUser().getUserId());
                data.put("authorities", loginUser.getAuthorities());
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                data.put("username", userDetails.getUsername());
                data.put("authorities", userDetails.getAuthorities());
            }
        } catch (Exception e) {
            // Anonymous
        }
        return data;
    }

    @Override
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Logged out successfully");
        return result;
    }
}
