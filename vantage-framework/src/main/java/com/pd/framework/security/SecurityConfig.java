package com.pd.framework.security;

import com.pd.common.event.auth.LoginFailureEvent;
import com.pd.common.event.auth.LoginSuccessEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final UserDetailsService userDetailsService;
	private final ApplicationEventPublisher eventPublisher;

	public SecurityConfig(UserDetailsService userDetailsService,
	                      ApplicationEventPublisher eventPublisher) {
		this.userDetailsService = userDetailsService;
		this.eventPublisher = eventPublisher;
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
							try {
								if (auth.getPrincipal() instanceof UserDetails userDetails) {
									eventPublisher.publishEvent(new LoginSuccessEvent(
											userDetails.getUsername(),
											getClientIp(req),
											"Unknown",
											getBrowser(req),
											getOS(req)
									));
								}
							} catch (Exception e) {
								// Log but don't fail login
							}
							res.setContentType("application/json");
							res.setStatus(200);
							res.getWriter().write("{\"code\":200,\"msg\":\"Login successful\"}");
						})
						.failureHandler((req, res, exc) -> {
							try {
								String username = req.getParameter("username");
								eventPublisher.publishEvent(new LoginFailureEvent(
										username != null ? username : "unknown",
										getClientIp(req),
										"Unknown",
										getBrowser(req),
										getOS(req),
										exc.getMessage()
								));
							} catch (Exception e) {
								// Log but don't fail
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
		String ua = request.getHeader("User-Agent");
		if (ua == null) return "Unknown";
		if (ua.contains("Edg")) return "Edge";
		if (ua.contains("Chrome")) return "Chrome";
		if (ua.contains("Firefox")) return "Firefox";
		if (ua.contains("Safari")) return "Safari";
		if (ua.contains("MSIE") || ua.contains("Trident")) return "IE";
		return "Unknown";
	}

	private String getOS(HttpServletRequest request) {
		String ua = request.getHeader("User-Agent");
		if (ua == null) return "Unknown";
		String lower = ua.toLowerCase();
		if (lower.contains("win")) return "Windows";
		if (lower.contains("mac")) return "Mac";
		if (lower.contains("linux")) return "Linux";
		if (lower.contains("android")) return "Android";
		if (lower.contains("iphone") || lower.contains("ipad")) return "iOS";
		return "Unknown";
	}
}
