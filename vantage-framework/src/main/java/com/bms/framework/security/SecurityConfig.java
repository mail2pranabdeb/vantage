package com.pd.framework.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                                                .requestMatchers("/api/login", "/api/logout", "/api/me").permitAll()
                                                .requestMatchers("/api/system/**", "/api/tool/**").authenticated()
                                                .requestMatchers("/system/**", "/tool/**").permitAll()
                                                .anyRequest().permitAll())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/api/login")
                                                .successHandler((req, res, auth) -> res.setStatus(200))
                                                .failureHandler((req, res, exc) -> res.setStatus(401))
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/api/logout")
                                                .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                                                .permitAll())
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }
}
