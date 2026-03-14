package com.pd.modules.system.security;

import com.pd.common.event.auth.LoginFailureEvent;
import com.pd.common.event.auth.LoginSuccessEvent;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final SysUserRepository userRepository;
    private final SysMenuRepository menuRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserDetailsServiceImpl(SysUserRepository userRepository, SysMenuRepository menuRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            SysUser user = userRepository.findByLoginName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

            Set<String> permissions;
            if (user.getUserId() == 1L) {
                permissions = Set.of(
                        "*:*:*",
                        "system:user:list", "system:user:query", "system:user:add", "system:user:edit", "system:user:remove",
                        "system:role:list", "system:role:query", "system:role:add", "system:role:edit", "system:role:remove",
                        "system:menu:list", "system:menu:query", "system:menu:add", "system:menu:edit", "system:menu:remove",
                        "system:config:list", "system:config:query", "system:config:add", "system:config:edit", "system:config:remove");
            } else {
                // For now, give all users the same permissions as admin (temporary for development)
                permissions = Set.of(
                        "*:*:*",
                        "system:user:list", "system:user:query", "system:user:add", "system:user:edit", "system:user:remove",
                        "system:role:list", "system:role:query", "system:role:add", "system:role:edit", "system:role:remove",
                        "system:menu:list", "system:menu:query", "system:menu:add", "system:menu:edit", "system:menu:remove",
                        "system:config:list", "system:config:query", "system:config:add", "system:config:edit", "system:config:remove");
            }
            
            // Publish login success event
            publishLoginSuccess(user);
            
            return new LoginUser(user, permissions);
            
        } catch (UsernameNotFoundException e) {
            // Publish login failure event
            publishLoginFailure(username, e.getMessage());
            throw e;
        } catch (Exception e) {
            // Publish login failure event
            publishLoginFailure(username, e.getMessage());
            throw new UsernameNotFoundException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Publish login success event
     */
    private void publishLoginSuccess(SysUser user) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            eventPublisher.publishEvent(new LoginSuccessEvent(
                user.getLoginName(),
                getClientIp(request),
                "",  // location (can be added with IP geolocation service)
                request.getHeader("User-Agent"),
                getOS(request.getHeader("User-Agent"))
            ));
        }
    }

    /**
     * Publish login failure event
     */
    private void publishLoginFailure(String username, String message) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            eventPublisher.publishEvent(new LoginFailureEvent(
                username,
                getClientIp(request),
                "",  // location
                request.getHeader("User-Agent"),
                getOS(request.getHeader("User-Agent")),
                message
            ));
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Extract OS from User-Agent
     */
    private String getOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.toLowerCase().contains("windows")) return "Windows";
        if (userAgent.toLowerCase().contains("mac")) return "Mac";
        if (userAgent.toLowerCase().contains("linux")) return "Linux";
        return "Unknown";
    }
}
