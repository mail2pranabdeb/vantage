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
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not available from " + registrationId);
        }

        SysUser user = userRepository.findByEmail(email).orElseGet(() -> {
            SysUser newUser = new SysUser();
            newUser.setLoginName(email);
            newUser.setUserName(name != null ? name : email.split("@")[0]);
            newUser.setEmail(email);
            newUser.setUserType("00");
            newUser.setSex("0");
            newUser.setStatus("0");
            newUser.setDelFlag("0");
            newUser.setPassword("");
            newUser.setCreateBy("oauth2");
            newUser.setCreateTime(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getUserId());
        attributes.put("loginName", user.getLoginName());

        Set<OAuth2UserAuthority> authorities = new HashSet<>();
        authorities.add(new OAuth2UserAuthority("ROLE_USER", attributes));

        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
