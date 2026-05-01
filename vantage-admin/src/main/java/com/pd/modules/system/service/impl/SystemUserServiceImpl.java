package com.pd.modules.system.service.impl;

import com.pd.common.event.user.UserCreatedEvent;
import com.pd.common.event.user.UserDeletedEvent;
import com.pd.modules.system.api.SystemUserService;
import com.pd.modules.system.api.dto.UserDTO;
import com.pd.modules.system.context.UserAuditContextHolder;
import com.pd.modules.system.domain.SysUser;
import com.pd.modules.system.infrastructure.repository.SysUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllActive() {
        return userRepository.findAllActive().stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long userId) {
        return userRepository.findById(userId).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByLoginName(String loginName) {
        return userRepository.findByLoginName(loginName).map(this::toDTO);
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        SysUser user = toEntity(userDTO);
        user.setDelFlag("0");
        user.setCreateTime(LocalDateTime.now());
        SysUser savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getUserId(), savedUser.getLoginName(), savedUser.getUserName()));
        return toDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        SysUser user = toEntity(userDTO);
        user.setUpdateTime(LocalDateTime.now());
        SysUser updatedUser = userRepository.save(user);
        UserAuditContextHolder.setAfterEntity(updatedUser);
        return toDTO(updatedUser);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        return userRepository.findById(userId).map(user -> {
            String loginName = user.getLoginName();
            user.setDelFlag("2");
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            eventPublisher.publishEvent(new UserDeletedEvent(userId, loginName));
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean deleteUserByIds(Long[] userIds) {
        for (Long id : userIds) {
            deleteUser(id);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoginName(String loginName) {
        return userRepository.findByLoginName(loginName).isPresent();
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(newPassword);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void changeStatus(Long userId, String status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Long[] roleIds) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRoleIds(roleIds);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> findUserWithRoles(Long userId) {
        Map<String, Object> result = new HashMap<>();
        userRepository.findById(userId).ifPresent(user -> {
            result.put("user", toDTO(user));
            result.put("roleIds", user.getRoleIds());
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public int countByStatus(String status) {
        return userRepository.countByStatus(status);
    }

    private UserDTO toDTO(SysUser entity) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysUser toEntity(UserDTO dto) {
        SysUser entity = new SysUser();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
