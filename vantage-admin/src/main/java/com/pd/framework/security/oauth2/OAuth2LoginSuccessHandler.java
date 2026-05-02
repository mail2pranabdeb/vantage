package com.pd.framework.security.oauth2;

import com.pd.framework.security.jwt.JwtTokenUtil;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.security.LoginUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil, SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        var userOpt = userRepository.findByEmail(email);
        SysUser sysUser;
        if (userOpt.isEmpty()) {
            sysUser = new SysUser();
            sysUser.setLoginName(email.substring(0, email.indexOf('@')));
            sysUser.setUserName(name != null ? name : email);
            sysUser.setEmail(email);
            sysUser.setUserType("00");
            sysUser.setStatus("0");
            sysUser.setDelFlag("0");
            sysUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            userRepository.save(sysUser);
        } else {
            sysUser = userOpt.get();
        }

        LoginUser loginUser = new LoginUser(sysUser, new HashSet<>());

        String token = jwtTokenUtil.generateToken(loginUser);
        String refreshToken = jwtTokenUtil.generateRefreshToken(loginUser);

        String redirectUrl = "/oauth2/callback#token=" + token +
                "&refresh=" + refreshToken +
                "&user=" + URLEncoder.encode(loginUser.getUsername(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
