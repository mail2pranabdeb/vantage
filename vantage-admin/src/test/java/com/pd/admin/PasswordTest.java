package com.pd.admin;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("----- BCRYPT HASH FOR 123456 -----");
        System.out.println(encoder.encode("123456"));
        System.out.println("----------------------------------");
        System.out.println("Does old hash match 123456? "
                + encoder.matches("123456", "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2"));
        System.out.println("Does old hash match admin123? "
                + encoder.matches("admin123", "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2"));
    }
}
