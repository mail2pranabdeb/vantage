package com.pd.modules.system.service.impl;

import com.pd.common.event.user.UserCreatedEvent;
import com.pd.common.event.user.UserDeletedEvent;
import com.pd.modules.system.api.SystemUserService;
import com.pd.modules.system.context.UserAuditContextHolder;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.persistence.EntityManager;
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
    private final ApplicationEventPublisher eventPublisher;

    public SystemUserServiceImpl(SysUserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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
        SysUser savedUser = userRepository.save(user);
        
        // Publish event after transaction commits
        eventPublisher.publishEvent(new UserCreatedEvent(
            savedUser.getUserId(), 
            savedUser.getLoginName(), 
            savedUser.getUserName()
        ));
        
        return savedUser;
    }

    @Override
    @Transactional
    public SysUser updateUser(SysUser user) {
        // Update the user
        user.setUpdateTime(LocalDateTime.now());
        SysUser updatedUser = userRepository.save(user);
        
        // Store after state in thread-local for AOP to capture
        UserAuditContextHolder.setAfterEntity(updatedUser);
        
        return updatedUser;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        Optional<SysUser> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            SysUser user = userOpt.get();
            String loginName = user.getLoginName();
            user.setDelFlag("2"); // Soft delete
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            // Publish event after transaction commits
            eventPublisher.publishEvent(new UserDeletedEvent(userId, loginName));
            
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
