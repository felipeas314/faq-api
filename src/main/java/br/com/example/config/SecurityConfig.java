package br.com.example.config;

import br.com.example.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Auth endpoints - públicos
                        .pathMatchers("/api/auth/**").permitAll()
                        // FAQ search - público
                        .pathMatchers(HttpMethod.POST, "/api/faqs/search").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/faqs/**").permitAll()
                        // FAQ management - apenas ADMIN
                        .pathMatchers(HttpMethod.POST, "/api/faqs").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/faqs/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/faqs/**").hasRole("ADMIN")
                        // Demais endpoints - autenticado
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
