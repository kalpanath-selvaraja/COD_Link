package com.ijse.codlink.security;

import com.ijse.codlink.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ---- Public Static Resources & Frontend ----
                        .requestMatchers(
                                "/",
                                "/login.html",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/css/**",
                                "/js/**",
                                "/admin/**",
                                "/customer/**",
                                "/images/**",
                                "/assets/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ---- Public Auth & APIs ----
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/parcels/track/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pricing-rules/**").permitAll()

                        // ---- Reference data — read-only (Public GET, Admin for modifications) ----
                        .requestMatchers(HttpMethod.GET, "/api/branches", "/api/branches/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/service-zones", "/api/service-zones/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/branches", "/api/branches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/branches", "/api/branches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/branches", "/api/branches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/service-zones", "/api/service-zones/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/service-zones", "/api/service-zones/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/service-zones", "/api/service-zones/**").hasRole("ADMIN")

                        // ---- Self-service
                        .requestMatchers(HttpMethod.GET, "/api/customers/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/customers/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/customers/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/parcels/me").authenticated()

                        // ---- Courier self-service — must precede the /api/couriers/** ADMIN
                        // rules below, since Spring Security uses first-match-wins ordering.
                        .requestMatchers(HttpMethod.GET, "/api/couriers/me").hasRole("COURIER")

                        // ---- Customers  ----
                        .requestMatchers(HttpMethod.POST, "/api/customers/walk-in").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/customers", "/api/customers/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/parcels/available", "/api/parcels/assigned").hasRole("COURIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/parcels/*/claim").hasRole("COURIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/parcels/*/courier/*").hasRole("ADMIN")

                        // ---- Parcels  ----
                        .requestMatchers(HttpMethod.GET, "/api/parcels").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/for-customer/**").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/parcels/*/status").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/parcels/*/shipment/*").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/parcels/*").hasRole("ADMIN")

                        // ---- Shipments  ----
                        .requestMatchers(HttpMethod.GET, "/api/shipments").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/shipments/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                        .requestMatchers(HttpMethod.GET, "/api/notification/me").authenticated()

                        // ---- ops entities  ----
                        .requestMatchers("/api/couriers", "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers("/api/vehicles", "/api/vehicles/**").hasRole("ADMIN")
                        .requestMatchers("/api/notification", "/api/notification/**").hasRole("ADMIN")
                        .requestMatchers("/api/audit-logs", "/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ---- PricingRule writes ----
                        .requestMatchers(HttpMethod.POST,"/api/couriers", "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/couriers", "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/couriers", "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pricing-rules").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pricing-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pricing-rules/**").hasRole("ADMIN")

                        // ---- Operational creates ----
                        .requestMatchers(HttpMethod.POST, "/api/tracking-events").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.POST, "/api/delivery-attempts").hasRole("COURIER")
                        .requestMatchers(HttpMethod.POST, "/api/branch-transfers").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.POST, "/api/cod-collections").hasRole("COURIER")
                        .requestMatchers(HttpMethod.POST, "/api/reconciliation-batches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/invoices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/delivery-attempts").hasAnyRole("ADMIN","COURIER")
                        .requestMatchers(HttpMethod.GET, "/api/tracking-events/**").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.GET, "/api/delivery-attempts/**").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.GET, "/api/branch-transfers/**").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.GET, "/api/cod-collections").hasAnyRole("ADMIN", "COURIER")
                        .requestMatchers(HttpMethod.GET, "/api/reconciliation-batches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/customers/link").authenticated()
                        .requestMatchers("/api/chat/**").hasAnyRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, error) -> response.sendError(401))
                        .accessDeniedHandler((request, response, error) -> response.sendError(403)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}