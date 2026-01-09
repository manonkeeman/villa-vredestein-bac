package com.villavredestein.config;

import com.villavredestein.security.JwtAuthenticationFilter;
import com.villavredestein.service.UserDetailsService;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // =====================================================================
    // CORS
    // =====================================================================

    /**
     * Dev-vriendelijk, maar niet ideaal voor productie.
     * Voor productie: vervang "*" door je echte frontend origins (bijv. https://villavredestein.com).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

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
                  \"message\": \"Authenticatie vereist of token ongeldig\"
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
                  \"message\": \"Je hebt geen toegang tot deze resource\"
                }
                """);
        };
    }

    // =====================================================================
    // SECURITY FILTER CHAIN
    // =====================================================================

    /**
     * Belangrijk:
     * - Rollen worden hier globaal afgedwongen.
     * - Eigenaarschap (student mag alleen eigen profiel) wordt afgedwongen in de service-laag
     *   via checks op de ingelogde gebruiker (SecurityContext).
     */
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

                        // Public auth endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/validate",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Users: alleen ingelogde rollen (ownership per userId gebeurt in service)
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "STUDENT", "CLEANER")

                        // Rooms (voorbeeld): admin/student mogen lezen, admin beheert
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")

                        // Alles wat overblijft moet ingelogd zijn
                        .anyRequest().authenticated()
                )

                // H2 console in frames toestaan (dev)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Spring Security moet jouw users kunnen laden
                .userDetailsService(userDetailsService)

                // JWT filter vóór username/password filter
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