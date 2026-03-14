package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysUser;
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

    @Query("SELECT u FROM SysUser u WHERE u.email = :email AND u.delFlag = '0'")
    Optional<SysUser> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM SysUser u WHERE u.phonenumber = :phonenumber AND u.delFlag = '0'")
    Optional<SysUser> findByPhonenumber(@Param("phonenumber") String phonenumber);

    @Query("SELECT u FROM SysUser u WHERE u.status = :status AND u.delFlag = '0'")
    List<SysUser> findByStatus(@Param("status") String status);
}
