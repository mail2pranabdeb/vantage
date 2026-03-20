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
        log.info("=== loadUserByUsername called for: {} ===", username);
        try {
            SysUser user = userRepository.findByLoginName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

            // Load permissions from database based on user's roles
            Set<String> permissions;
            try {
                permissions = menuRepository.findMenuPermsByUserId(user.getUserId());
                log.info("=== User {} has {} permissions ===", username, permissions.size());
            } catch (Exception e) {
                log.error("=== Failed to load permissions from DB: {} ===", e.getMessage());
                log.info("=== Falling back to default admin permissions ===");
                // Fallback: give admin user all permissions
                if (user.getUserId() == 1L) {
                    permissions = Set.of("*:*:*", 
                        "system:user:list", "system:user:query", "system:user:add", "system:user:edit", "system:user:remove",
                        "system:role:list", "system:role:query", "system:role:add", "system:role:edit", "system:role:remove",
                        "system:menu:list", "system:menu:query", "system:menu:add", "system:menu:edit", "system:menu:remove",
                        "system:config:list", "system:config:query", "system:config:add", "system:config:edit", "system:config:remove",
                        "system:dict:list", "system:dict:query", "system:dict:add", "system:dict:edit", "system:dict:remove", "system:dict:export",
                        "system:post:list", "system:post:query", "system:post:add", "system:post:edit", "system:post:remove",
                        "monitor:operlog:list", "monitor:operlog:query", "monitor:operlog:remove", "monitor:operlog:export",
                        "monitor:logininfor:list", "monitor:logininfor:query", "monitor:logininfor:remove", "monitor:logininfor:export",
                        "monitor:job:list", "monitor:job:query", "monitor:job:add", "monitor:job:edit", "monitor:job:remove", "monitor:job:run",
                        "monitor:jobLog:list", "monitor:jobLog:query", "monitor:jobLog:remove",
                        "tool:gen:list", "tool:gen:query", "tool:gen:add", "tool:gen:edit", "tool:gen:remove", "tool:gen:import", "tool:gen:preview", "tool:gen:code",
                        "system:notice:list", "system:notice:query", "system:notice:add", "system:notice:edit", "system:notice:remove"
                    );
                } else {
                    permissions = Set.of();
                }
            }
            
            // If no permissions found, return empty set
            if (permissions == null || permissions.isEmpty()) {
                log.warn("=== WARNING: User {} has NO permissions! ===", username);
                permissions = Set.of();
            }
            
            log.info("=== User {} authenticated successfully, publishing LoginSuccessEvent ===", username);
            // Publish login success event
            publishLoginSuccess(user);

            return new LoginUser(user, permissions);

        } catch (UsernameNotFoundException e) {
            log.info("=== User {} not found, publishing LoginFailureEvent ===", username);
            // Publish login failure event
            publishLoginFailure(username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("=== Authentication failed for {}: {}, publishing LoginFailureEvent ===", username, e.getMessage());
            // Publish login failure event
            publishLoginFailure(username, e.getMessage());
            throw new UsernameNotFoundException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Publish login success event
     */
    private void publishLoginSuccess(SysUser user) {
        log.info("=== publishLoginSuccess called for user: {} ===", user.getLoginName());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            LoginSuccessEvent event = new LoginSuccessEvent(
                user.getLoginName(),
                getClientIp(request),
                "",  // location (can be added with IP geolocation service)
                request.getHeader("User-Agent"),
                getOS(request.getHeader("User-Agent"))
            );
            log.info("=== Publishing LoginSuccessEvent: user={}, ip={} ===", event.getLoginName(), event.getIpAddress());
            eventPublisher.publishEvent(event);
            log.info("=== LoginSuccessEvent published ===");
        } else {
            log.warn("=== No request attributes available, cannot publish LoginSuccessEvent ===");
        }
    }

    /**
     * Publish login failure event
     */
    private void publishLoginFailure(String username, String message) {
        log.info("=== publishLoginFailure called for user: {} ===", username);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            LoginFailureEvent event = new LoginFailureEvent(
                username,
                getClientIp(request),
                "",  // location
                request.getHeader("User-Agent"),
                getOS(request.getHeader("User-Agent")),
                message
            );
            log.info("=== Publishing LoginFailureEvent: user={}, ip={}, msg={} ===", event.getLoginName(), event.getIpAddress(), event.getMessage());
            eventPublisher.publishEvent(event);
            log.info("=== LoginFailureEvent published ===");
        } else {
            log.warn("=== No request attributes available, cannot publish LoginFailureEvent ===");
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
