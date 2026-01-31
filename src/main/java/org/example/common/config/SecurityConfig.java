package org.example.common.config;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Bean
        public JwtDecoder jwtDecoder() {
                log.info("🔑 JWT_SECRET length: {}, first 10 chars: {}",
                                jwtSecret.length(),
                                jwtSecret.substring(0, Math.min(10, jwtSecret.length())));
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                // ✅ 알고리즘을 명시적으로 HS256으로 지정
                return NimbusJwtDecoder.withSecretKey(key)
                                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // 🔥 핵심: CORS 설정
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // 🔥 Flutter Web 대응
                config.addAllowedOriginPattern("*");

                config.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Authorization"));

                // ❗ 이거 true면 Flutter Web에서 막힘
                config.setAllowCredentials(false);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        log.error("Unauthorized error: {}", authException.getMessage());
                                                        response.sendError(401, authException.getMessage());
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        log.error("Access denied error: {}",
                                                                        accessDeniedException.getMessage());
                                                        response.sendError(403, accessDeniedException.getMessage());
                                                }))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/ping",
                                                                "/actuator/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs.yaml",
                                                                "/auth/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                                }))
                                .httpBasic(AbstractHttpConfigurer::disable);

                return http.build();
        }
}
