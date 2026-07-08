package com.smartHome.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        .requestMatchers("/api/auth/user").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()

                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/scenes/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/scenes/*/run").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/scenes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/scenes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/scenes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/scenes/**").hasRole("ADMIN")

                        .requestMatchers("/api/automations/**").hasRole("ADMIN")

                        .requestMatchers("/api/lights/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/ac/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/tuya/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/tapo/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/devices/**").hasAnyRole("ADMIN", "USER")

                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://192.168.0.*:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}