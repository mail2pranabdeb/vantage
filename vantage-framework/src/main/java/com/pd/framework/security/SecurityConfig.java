package com.pd.framework.security;

import com.pd.common.event.auth.LoginFailureEvent;
import com.pd.common.event.auth.LoginSuccessEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final UserDetailsService userDetailsService;

	// Spring will auto-inject the UserDetailsService bean from system module
	public SecurityConfig(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authz -> authz
						.requestMatchers("/api/login", "/api/logout").permitAll()
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/system/**").authenticated()
						.requestMatchers("/tool/**").authenticated()
						.anyRequest().permitAll())
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/api/login")
						.successHandler((req, res, auth) -> {
							// Publish login success event
							try {
								ApplicationEventPublisher publisher = 
									(ApplicationEventPublisher) req.getServletContext()
										.getAttribute(ApplicationEventPublisher.class.getName());
								if (publisher != null && auth.getPrincipal() instanceof UserDetails) {
									UserDetails userDetails = (UserDetails) auth.getPrincipal();
									String ip = getClientIp(req);
									publisher.publishEvent(new LoginSuccessEvent(
										userDetails.getUsername(),
										ip,
										"Unknown",
										getBrowser(req),
										getOS(req)
									));
								}
							} catch (Exception e) {
								// Ignore event publishing errors
							}
							
							res.setContentType("application/json");
							res.setStatus(200);
							res.getWriter().write("{\"code\":200,\"msg\":\"Login successful\"}");
						})
						.failureHandler((req, res, exc) -> {
							// Publish login failure event
							try {
								ApplicationEventPublisher publisher = 
									(ApplicationEventPublisher) req.getServletContext()
										.getAttribute(ApplicationEventPublisher.class.getName());
								if (publisher != null) {
									String username = req.getParameter("username");
									String ip = getClientIp(req);
									publisher.publishEvent(new LoginFailureEvent(
										username != null ? username : "unknown",
										ip,
										"Unknown",
										getBrowser(req),
										getOS(req),
										exc.getMessage()
									));
								}
							} catch (Exception e) {
								// Ignore event publishing errors
							}
							
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
				.authenticationProvider(authenticationProvider())
				.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.disable()));

		return http.build();
	}
	
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if ("0:0:0:0:0:0:0:1".equals(ip) || "0.0.0.0".equals(ip)) {
			ip = "127.0.0.1";
		}
		return ip;
	}
	
	private String getBrowser(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null) return "Unknown";
		if (userAgent.contains("Chrome")) return "Chrome";
		if (userAgent.contains("Firefox")) return "Firefox";
		if (userAgent.contains("Edge")) return "Edge";
		if (userAgent.contains("Safari")) return "Safari";
		if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
		return "Unknown";
	}
	
	private String getOS(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null) return "Unknown";
		if (userAgent.toLowerCase().contains("win")) return "Windows";
		if (userAgent.toLowerCase().contains("mac")) return "Mac";
		if (userAgent.toLowerCase().contains("linux")) return "Linux";
		return "Unknown";
	}
}
