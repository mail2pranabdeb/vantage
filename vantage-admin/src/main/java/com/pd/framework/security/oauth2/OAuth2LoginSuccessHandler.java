package com.pd.framework.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.framework.security.jwt.JwtTokenUtil;
import com.pd.framework.security.jwt.LoginResponse;
import com.pd.modules.system.security.LoginUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil, SysUserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"msg\":\"User not found after OAuth2 login\"}");
            return;
        }

        var sysUser = userOpt.get();
        LoginUser loginUser = new LoginUser(sysUser, new java.util.HashSet<>());

        String token = jwtTokenUtil.generateToken(loginUser);
        String refreshToken = jwtTokenUtil.generateRefreshToken(loginUser);

        LoginResponse loginResponse = new LoginResponse(
                token, refreshToken, "Bearer",
                86400000, loginUser.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "Login successful");
        result.put("data", loginResponse);

        response.setContentType("application/json");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getWriter(), result);
    }
}
