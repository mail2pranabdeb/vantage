package com.pd.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping({"/oauth2/callback", "/auth/google/callback"})
    public void redirect(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Preserve the hash fragment from the original request if present
        String queryString = request.getQueryString();
        String url = frontendUrl + "/auth/google/callback";
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        // Note: hash fragments are never sent to the server, but if the browser follows
        // a redirect with a hash, it will preserve it
        response.sendRedirect(url);
    }
}
