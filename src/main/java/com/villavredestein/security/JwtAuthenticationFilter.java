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

        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/auth")
                || path.startsWith("/h2-console")
                || path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring("Bearer ".length()).trim();
        if (jwtToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email;
        try {
            email = jwtService.extractEmail(jwtToken); // <— PAS DIT AAN als jouw JwtService anders heet
        } catch (Exception e) {
            log.warn("JWT parsing failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null || email.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isValid;
        try {
            isValid = jwtService.validateToken(jwtToken); // <— 1 parameter (zoals jouw compile error liet zien)
        } catch (Exception e) {
            log.warn("JWT validation failed for {}: {}", maskEmail(email), e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (!isValid) {
            log.warn("Invalid JWT token for user {}", maskEmail(email));
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            log.warn("User referenced in token does not exist: {}", maskEmail(email));
            filterChain.doFilter(request, response);
            return;
        }

        if (userDetails.getUsername() == null || !userDetails.getUsername().equalsIgnoreCase(email)) {
            log.warn("JWT subject mismatch (token={}, userDetails={})",
                    maskEmail(email), maskEmail(userDetails.getUsername()));
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 1) return "***";
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }
}