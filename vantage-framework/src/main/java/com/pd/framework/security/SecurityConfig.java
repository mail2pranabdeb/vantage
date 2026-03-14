package com.pd.framework.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * Note: LoginUser has been moved to com.pd.modules.system.security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/api/login", "/api/logout").permitAll()
                                                .requestMatchers("/api/**").authenticated()
                                                .requestMatchers("/system/**").authenticated()
                                                .requestMatchers("/tool/**").authenticated()
                                                .anyRequest().permitAll())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/api/login")
                                                .successHandler((req, res, auth) -> {
                                                        res.setContentType("application/json");
                                                        res.setStatus(200);
                                                        res.getWriter().write("{\"code\":200,\"msg\":\"Login successful\"}");
                                                })
                                                .failureHandler((req, res, exc) -> {
                                                        res.setContentType("application/json");
                                                        res.setStatus(401);
                                                        res.getWriter().write("{\"code\":401,\"msg\":\"Invalid credentials\"}");
                                                })
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/api/logout")
                                                .logoutSuccessHandler((req, res, auth) -> {
                                                        res.setStatus(200);
                                                        res.getWriter().write("{\"code\":200,\"msg\":\"Logged out\"}");
                                                })
                                                .permitAll())
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }
}
