package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemUserService;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SystemUserService API.
 * This is the public implementation that external modules should use.
 */
@Service
public class SystemUserServiceImpl implements SystemUserService {

    private final SysUserRepository userRepository;

    public SystemUserServiceImpl(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysUser> findAllActive() {
        return userRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysUser> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysUser> findByLoginName(String loginName) {
        return userRepository.findByLoginName(loginName);
    }

    @Override
    @Transactional
    public SysUser createUser(SysUser user) {
        user.setDelFlag("0");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public SysUser updateUser(SysUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        Optional<SysUser> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            SysUser user = userOpt.get();
            user.setDelFlag("2"); // Soft delete
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoginName(String loginName) {
        return userRepository.findByLoginName(loginName).isPresent();
    }
}
