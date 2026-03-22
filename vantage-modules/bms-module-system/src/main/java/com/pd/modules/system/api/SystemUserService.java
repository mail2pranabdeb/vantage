package com.pd.modules.system.api;

import com.pd.modules.system.domain.SysUser;
import java.util.List;
import java.util.Optional;

/**
 * System module public API for user operations.
 * This interface defines the contract for external modules to interact with the system module.
 */
public interface SystemUserService {

    /**
     * Get all active users
     * @return list of active users
     */
    List<SysUser> findAllActive();

    /**
     * Get user by ID
     * @param userId the user ID
     * @return optional containing the user if found
     */
    Optional<SysUser> findById(Long userId);

    /**
     * Get user by login name
     * @param loginName the login name
     * @return optional containing the user if found
     */
    Optional<SysUser> findByLoginName(String loginName);

    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     */
    SysUser createUser(SysUser user);

    /**
     * Update an existing user
     * @param user the user to update
     * @return the updated user
     */
    SysUser updateUser(SysUser user);

    /**
     * Delete a user (soft delete)
     * @param userId the user ID to delete
     * @return true if deleted successfully
     */
    boolean deleteUser(Long userId);

    /**
     * Check if login name exists
     * @param loginName the login name to check
     * @return true if exists
     */
    boolean existsByLoginName(String loginName);
}
