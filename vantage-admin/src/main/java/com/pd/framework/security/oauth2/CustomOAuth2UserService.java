package com.pd.framework.security.oauth2;

import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final SysUserRepository userRepository;

    public CustomOAuth2UserService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oAuth2User, registrationId, userRequest);
        String name = extractName(oAuth2User, registrationId);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not available from " + registrationId);
        }

        SysUser user = userRepository.findByEmail(email).orElseGet(() -> {
            SysUser newUser = new SysUser();
            newUser.setLoginName(email.contains("@") ? email.substring(0, email.indexOf('@')) : email);
            newUser.setUserName(name != null ? name : email);
            newUser.setEmail(email);
            newUser.setUserType("00");
            newUser.setSex("0");
            newUser.setStatus("0");
            newUser.setDelFlag("0");
            newUser.setPassword("");
            newUser.setCreateBy("oauth2-" + registrationId);
            newUser.setCreateTime(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email);
        attributes.put("userId", user.getUserId());
        attributes.put("loginName", user.getLoginName());

        Set<OAuth2UserAuthority> authorities = new HashSet<>();
        authorities.add(new OAuth2UserAuthority("ROLE_USER", attributes));

        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId, OAuth2UserRequest userRequest) {
        String email = oAuth2User.getAttribute("email");
        if (email != null) return email;

        if ("github".equals(registrationId)) {
            String githubEmail = fetchGitHubEmail(userRequest);
            if (githubEmail != null) return githubEmail;
            // Fallback: use noreply format so login can proceed
            String login = oAuth2User.getAttribute("login");
            if (login != null) {
                log.info("GitHub email not available, using noreply format for user: {}", login);
                return login + "@users.noreply.github.com";
            }
        }
        return null;
    }

    private String fetchGitHubEmail(OAuth2UserRequest userRequest) {
        try {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            java.net.URL url = new java.net.URL("https://api.github.com/user/emails");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            conn.setRequestProperty("User-Agent", "Vantage-App");

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("GitHub email API returned status: {}", status);
                return null;
            }

            try (java.io.InputStream is = conn.getInputStream();
                 java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                
                // Parse JSON array: [{"email": "...", "primary": true, "verified": true, "visibility": "..."}]
                String body = sb.toString();
                // Simple parsing for the first verified primary email
                if (body.contains("primary\":true")) {
                    int idx = body.indexOf("\"primary\":true");
                    // Find email before or after this
                    int emailStart = body.lastIndexOf("\"email\":\"", idx);
                    if (emailStart != -1) {
                        emailStart += 9; // length of "email":"
                        int emailEnd = body.indexOf("\"", emailStart);
                        if (emailEnd != -1) {
                            return body.substring(emailStart, emailEnd);
                        }
                    }
                }
                // Fallback: just find the first email in the array
                int emailStart = body.indexOf("\"email\":\"");
                if (emailStart != -1) {
                    emailStart += 9;
                    int emailEnd = body.indexOf("\"", emailStart);
                    if (emailEnd != -1) {
                        return body.substring(emailStart, emailEnd);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub email: {}", e.getMessage());
        }
        return null;
    }

    private String extractName(OAuth2User oAuth2User, String registrationId) {
        String name = oAuth2User.getAttribute("name");
        if (name != null) return name;
        
        // GitHub fallback
        if ("github".equals(registrationId)) {
            String login = oAuth2User.getAttribute("login");
            if (login != null) return login;
        }
        return "Unknown";
    }
}
