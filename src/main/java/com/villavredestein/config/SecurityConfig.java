package com.villavredestein.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ==========================================
    // Gebruikersbeheer (in-memory)
    // ==========================================
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build();

        UserDetails student = User.builder()
                .username("student")
                .password("{noop}student123")
                .roles("STUDENT")
                .build();

        UserDetails cleaner = User.builder()
                .username("cleaner")
                .password("{noop}cleaner123")
                .roles("CLEANER")
                .build();

        return new InMemoryUserDetailsManager(admin, student, cleaner);
    }

    // ==========================================
    // Beveiligingsregels
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers(
                                "/api/documents/ping",
                                "/api/payments/ping",
                                "/api/tasks/ping",
                                "/api/assignments/ping"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}