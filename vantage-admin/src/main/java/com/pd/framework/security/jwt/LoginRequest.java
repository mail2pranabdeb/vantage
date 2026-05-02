package com.pd.framework.security.jwt;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
