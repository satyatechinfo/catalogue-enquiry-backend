
package com.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
			throws Exception {

		http.cors(cors -> cors.configurationSource(corsConfigurationSource)).csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth

						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						.requestMatchers("/", "/error").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**", "/api/categories",
								"/api/occasions")
						.permitAll()

						.requestMatchers(HttpMethod.POST, "/api/enquiries").permitAll()

						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()

						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						.anyRequest().denyAll())
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
			@Value("${app.admin.username:admin}") String username, @Value("${app.admin.password:}") String password) {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

		if (StringUtils.hasText(password)) {
			manager.createUser(User.withUsername(username).password(passwordEncoder.encode(password.trim()))
					.roles("ADMIN").build());

			log.info("Admin endpoints enabled for user '{}'", username);
		} else {
			log.warn("app.admin.password is not set - admin endpoints are disabled");
		}

		return manager;
	}

	@Bean
CorsConfigurationSource corsConfigurationSource(
        @Value("${app.cors.allowed-origins:}") String allowedOrigins) {

    List<String> origins = new java.util.ArrayList<>(
            Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                    .filter(StringUtils::hasText)
                    .toList());

    // Always allow the production frontend, even if the env var is stale
    origins.add("https://catalogue-enquiry-customer-frontend.vercel.app");

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origins.stream().distinct().toList());
    config.setAllowedOriginPatterns(List.of(
            "https://catalogue-enquiry-*.vercel.app",
            "http://localhost:*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setMaxAge(3600L);

    log.info("CORS allowed origins: {}", config.getAllowedOrigins());
    log.info("CORS allowed origin patterns: {}", config.getAllowedOriginPatterns());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
}
