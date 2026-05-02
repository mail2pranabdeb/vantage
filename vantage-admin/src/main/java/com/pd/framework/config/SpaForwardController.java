package com.pd.framework.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards SPA routes to index.html so React Router can handle them.
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/dashboard", "/oauth2/callback"})
    public String forward() {
        return "forward:/index.html";
    }
}
