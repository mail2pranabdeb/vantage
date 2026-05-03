package com.pd.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * Fallback controller for OAuth2 callback.
 * If the browser somehow lands on the backend instead of the frontend,
 * this safely redirects to the correct frontend URL.
 */
@Controller
public class OAuth2CallbackRedirector {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/auth/{provider}/callback")
    public void redirect(@PathVariable String provider, jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Preserve the query string from the original request
        String queryString = request.getQueryString();
        String url = frontendUrl + "/auth/" + provider + "/callback";
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        response.sendRedirect(url);
    }
}
