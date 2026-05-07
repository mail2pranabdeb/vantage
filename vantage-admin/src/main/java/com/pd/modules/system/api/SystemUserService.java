package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SystemUserService {

    List<UserDTO> findAll();

    List<UserDTO> findAllActive();

    Page<UserDTO> findAllActive(Pageable pageable);

    Page<UserDTO> searchUsers(String loginName, String status, Pageable pageable);

    Optional<UserDTO> findById(Long userId);

    Optional<UserDTO> findByLoginName(String loginName);

    UserDTO createUser(UserDTO user);

    UserDTO updateUser(UserDTO user);

    boolean deleteUser(Long userId);

    boolean deleteUserByIds(Long[] userIds);

    boolean existsByLoginName(String loginName);

    void resetPassword(Long userId, String newPassword);

    void changeStatus(Long userId, String status);

    void assignRole(Long userId, Long[] roleIds);

    Map<String, Object> findUserWithRoles(Long userId);

    int countByStatus(String status);
}
