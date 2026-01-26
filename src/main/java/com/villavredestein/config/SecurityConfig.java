package com.villavredestein.config;

import com.villavredestein.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,https://*.netlify.app}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // =====================================================================
    // CORS
    // =====================================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        String rawOrigins = allowedOrigins;
        List<String> originPatterns = Arrays.stream(rawOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        config.setAllowedOriginPatterns(originPatterns);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // =====================================================================
    // 401 / 403 HANDLERS
    // =====================================================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  \"status\": 401,
                  \"error\": \"Unauthorized\",
                  \"message\": \"Authentication required or token is invalid\"
                }
                """);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  \"status\": 403,
                  \"error\": \"Forbidden\",
                  \"message\": \"You do not have access to this resource\"
                }
                """);
        };
    }

    // =====================================================================
    // SECURITY FILTER CHAIN
    // =====================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                // Auth endpoints (both with and without /api prefix)
                                "/auth/**",
                                "/api/auth/**",

                                // API docs
                                "/swagger-ui/**",
                                "/v3/api-docs/**",

                                // Local dev console
                                "/h2-console/**"
                        ).permitAll()

                        // Public registration endpoints (adjust if your controller uses a different path)
                        .requestMatchers(HttpMethod.POST,
                                "/users",
                                "/users/register",
                                "/api/users",
                                "/api/users/register"
                        ).permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/cleaning/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.POST, "/api/cleaning/**").hasAnyRole("ADMIN", "CLEANER")
                        .requestMatchers(HttpMethod.PUT, "/api/cleaning/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.PATCH, "/api/cleaning/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.DELETE, "/api/cleaning/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/documents/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")
                        .requestMatchers(HttpMethod.POST, "/api/documents/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/documents/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/documents/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================================
    // AUTH / PASSWORD BEANS
    // =====================================================================

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}