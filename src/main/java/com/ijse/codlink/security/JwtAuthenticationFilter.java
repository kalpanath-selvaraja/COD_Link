package com.ijse.codlink.security;

import com.ijse.codlink.responce.CommonResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null || username.isBlank()) {
                handleJwtException(response, 401, "Invalid token subject");
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }else {
                    handleJwtException(response, 401, "Invalid token");
                    return;
                }
            }

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT on {} {} from {}: {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());
            handleJwtException(response, 401, "Token expired");
            return;
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature on {} {} from {} - possible tampering: {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());
            handleJwtException(response, 401, "Invalid token signature");
            return;
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT on {} {} from {}: {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());
            handleJwtException(response, 401, "Invalid token format");
            return;

        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            log.warn("JWT referenced unknown or inactive account on {} {} from {}: {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());
            handleJwtException(response, 401, "Account not found or inactive");
            return;

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            handleJwtException(response, 401, "Invalid token");
            return;
        } catch (Exception ex) {
            log.error("Unexpected authentication failure on {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex);
            handleJwtException(response, 500, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        CommonResponse errorResponse = new CommonResponse(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }





}
