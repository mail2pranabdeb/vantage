package com.pd.framework.security;

import com.pd.framework.security.jwt.JwtAuthenticationFilter;
import com.pd.framework.security.oauth2.CustomOAuth2UserService;
import com.pd.framework.security.oauth2.FrontendAwareAuthRequestResolver;
import com.pd.framework.security.oauth2.OAuth2LoginSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Security configuration with JWT authentication and OAuth2 login support.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	private final UserDetailsService userDetailsService;
	private final ApplicationEventPublisher eventPublisher;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final ClientRegistrationRepository clientRegistrationRepository;

	public SecurityConfig(UserDetailsService userDetailsService,
	                      ApplicationEventPublisher eventPublisher,
	                      JwtAuthenticationFilter jwtAuthenticationFilter,
	                      CustomOAuth2UserService customOAuth2UserService,
	                      OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
	                      ClientRegistrationRepository clientRegistrationRepository) {
		this.userDetailsService = userDetailsService;
		this.eventPublisher = eventPublisher;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.customOAuth2UserService = customOAuth2UserService;
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
		this.clientRegistrationRepository = clientRegistrationRepository;
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
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authz -> authz
						.requestMatchers(
								"/api/login",
								"/api/login/refresh",
								"/api/logout",
								"/api/oauth2/**",
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/webjars/**",
								"/h2-console/**",
								"/actuator/**",
								"/api/public/**"
						).permitAll()
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/system/**").authenticated()
						.requestMatchers("/tool/**").authenticated()
						.anyRequest().permitAll())
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(new FrontendAwareAuthRequestResolver(clientRegistrationRepository)))
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
					.successHandler(oAuth2LoginSuccessHandler)
					.failureHandler((req, res, exc) -> {
						log.error("OAuth2 login failed: {}", exc.getMessage());
						res.sendRedirect("http://localhost:5173/login?oauth2_error=1");
					}))
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutUrl("/api/logout")
						.logoutSuccessHandler((req, res, auth) -> {
							res.setContentType("application/json");
							res.setStatus(200);
							res.getWriter().write("{\"code\":200,\"msg\":\"Logged out\"}");
						}))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
