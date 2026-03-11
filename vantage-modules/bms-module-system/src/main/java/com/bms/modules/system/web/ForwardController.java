package com.pd.modules.system.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForwardController {

    // Forward SPA UI routes to React's index.html
    @GetMapping({
            "/login",
            "/dashboard",
            "/dashboard/**",
            "/system/user",
            "/system/role",
            "/system/menu",
            "/system/config"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
