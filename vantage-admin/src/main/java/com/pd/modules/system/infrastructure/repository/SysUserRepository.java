package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    @Query("SELECT u FROM SysUser u WHERE u.loginName = :loginName AND u.delFlag = '0'")
    Optional<SysUser> findByLoginName(@Param("loginName") String loginName);

    @Query("SELECT u FROM SysUser u WHERE u.delFlag = '0'")
    List<SysUser> findAllActive();

    @Query("SELECT u FROM SysUser u WHERE u.delFlag = '0'")
    Page<SysUser> findAllActive(Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.delFlag = '0' AND (:loginName IS NULL OR u.loginName LIKE CONCAT('%', :loginName, '%')) AND (:status IS NULL OR u.status = :status)")
    Page<SysUser> searchActive(@Param("loginName") String loginName, @Param("status") String status, Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.email = :email AND u.delFlag = '0' ORDER BY u.userId ASC")
    List<SysUser> findAllByEmail(@Param("email") String email);

    default Optional<SysUser> findByEmail(String email) {
        List<SysUser> users = findAllByEmail(email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Query("SELECT u FROM SysUser u WHERE u.phonenumber = :phonenumber AND u.delFlag = '0'")
    Optional<SysUser> findByPhonenumber(@Param("phonenumber") String phonenumber);

    @Query("SELECT COUNT(u) FROM SysUser u WHERE u.status = :status AND u.delFlag = '0'")
    int countByStatus(@Param("status") String status);
}
