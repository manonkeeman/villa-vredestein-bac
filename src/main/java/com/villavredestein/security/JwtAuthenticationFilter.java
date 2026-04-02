package com.villavredestein.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isSkippableRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (jwtToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = extractEmailSafely(jwtToken);
        if (email == null || email.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isValidToken(jwtToken, email)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = loadUserSafely(email);
        if (userDetails == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!matchesUser(email, userDetails)) {
            log.warn("JWT subject mismatch (token={}, userDetails={})",
                    maskEmail(email), maskEmail(userDetails.getUsername()));
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private boolean isSkippableRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return "OPTIONS".equalsIgnoreCase(method)
                || path.startsWith("/api/auth")
                || path.startsWith("/h2-console")
                || path.startsWith("/error")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/healthz")
                || path.startsWith("/actuator/info");
    }

    private String extractEmailSafely(String jwtToken) {
        try {
            return jwtService.extractEmail(jwtToken);
        } catch (Exception exception) {
            log.warn("JWT parsing failed: {}", exception.getMessage());
            return null;
        }
    }

    private boolean isValidToken(String jwtToken, String email) {
        try {
            boolean isValid = jwtService.validateToken(jwtToken);
            if (!isValid) {
                log.warn("Invalid JWT token for user {}", maskEmail(email));
            }
            return isValid;
        } catch (Exception exception) {
            log.warn("JWT validation failed for {}: {}", maskEmail(email), exception.getMessage());
            return false;
        }
    }

    private UserDetails loadUserSafely(String email) {
        try {
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception exception) {
            log.warn("User referenced in token does not exist: {}", maskEmail(email));
            return null;
        }
    }

    private boolean matchesUser(String email, UserDetails userDetails) {
        return userDetails.getUsername() != null
                && userDetails.getUsername().equalsIgnoreCase(email);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(no-email)";
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');

        if (atIndex <= 1) {
            return "***";
        }

        return trimmed.charAt(0) + "***" + trimmed.substring(atIndex);
    }
}