package com.pd.gateway.system;

import com.pd.common.core.domain.AjaxResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/me")
    public AjaxResult me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return AjaxResult.error("Not authenticated");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", userDetails.getUsername());
        data.put("authorities", userDetails.getAuthorities());
        
        return AjaxResult.success(data);
    }

    @PostMapping("/logout")
    public AjaxResult logout() {
        return AjaxResult.success("Logged out successfully");
    }
}
