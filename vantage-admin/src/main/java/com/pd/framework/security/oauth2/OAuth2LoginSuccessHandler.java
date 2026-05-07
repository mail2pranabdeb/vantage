package com.pd.framework.security.oauth2;

import com.pd.framework.security.jwt.JwtTokenUtil;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.security.LoginUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserRepository userRepository;

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil, SysUserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Value("${app.frontend-url:http://localhost:5173}")
    private String configuredFrontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String state = request.getParameter("state");
        String frontendUrl = null;
        if (state != null && state.contains("|")) {
            frontendUrl = state.substring(0, state.indexOf("|"));
        }

        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = configuredFrontendUrl;
        }
        log.info("OAuth2 success: state={}, resolved frontendUrl={}", state, frontendUrl);

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Extract provider from the Authentication object
        String registrationId = "unknown";
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
            registrationId = oauthToken.getAuthorizedClientRegistrationId();
        }

        String emailPrefix = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String loginName = emailPrefix != null && emailPrefix.length() > 30 ? emailPrefix.substring(0, 30) : emailPrefix;

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() && loginName != null) {
            userOpt = userRepository.findByLoginName(loginName);
        }

        SysUser sysUser;
        if (userOpt.isEmpty()) {
            sysUser = new SysUser();
            sysUser.setLoginName(loginName);
            String rawName = name != null ? name : email;
            sysUser.setUserName(rawName.length() > 30 ? rawName.substring(0, 30) : rawName);
            sysUser.setEmail(email);
            sysUser.setUserType("00");
            sysUser.setStatus("0");
            sysUser.setDelFlag("0");
            sysUser.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(sysUser);
        } else {
            sysUser = userOpt.get();
        }

        LoginUser loginUser = new LoginUser(sysUser, new HashSet<>());

        String token = jwtTokenUtil.generateToken(loginUser);
        String refreshToken = jwtTokenUtil.generateRefreshToken(loginUser);

        // Store tokens in a short-lived in-memory store and pass only a code in the URL
        String code = OAuth2TokenStore.generateCode(token, refreshToken);
        String redirectUrl = frontendUrl + "/auth/" + registrationId + "/callback?code=" + code;
        log.info("=== OAuth2 REDIRECT (provider={}) to: {} ===", registrationId, redirectUrl.substring(0, Math.min(150, redirectUrl.length())));
        response.sendRedirect(redirectUrl);
    }
}
