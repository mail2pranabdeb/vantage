package com.pd.modules.system.security;

import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysMenuRepository;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserRepository userRepository;
    private final SysMenuRepository menuRepository;

    public UserDetailsServiceImpl(SysUserRepository userRepository, SysMenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
            permissions = menuRepository.findPermsByUserId(user.getUserId()).stream().collect(Collectors.toSet());
        }
        return new LoginUser(user, permissions);
    }
}
