package com.pd.modules.system.security;

import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final SysUserRepository userRepository;
    private final SysMenuRepository menuRepository;

    public UserDetailsServiceImpl(SysUserRepository userRepository, SysMenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== loadUserByUsername called for: {} ===", username);
        try {
            // Try loginName first, fallback to email (for OAuth2 users)
            SysUser user = userRepository.findByLoginName(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

            Set<String> permissions = menuRepository.findAllPermsList().stream()
                .flatMap(p -> Arrays.stream(p.split(",")))
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toSet());
            
            log.info("=== User {} loaded with {} permissions ===", username, permissions.size());
            if (permissions.isEmpty()) {
                log.error("=== ERROR: No permissions in database! Check sys_menu table! ===");
            }
            
            if (permissions == null || permissions.isEmpty()) {
                log.warn("=== WARNING: No permissions found in database! ===");
                permissions = Set.of();
            }
            
            log.info("=== User {} authenticated successfully ===", username);

            return new LoginUser(user, permissions);

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("=== Authentication failed for {}: {} ===", username, e.getMessage());
            throw new UsernameNotFoundException("Authentication failed: " + e.getMessage());
        }
    }
}