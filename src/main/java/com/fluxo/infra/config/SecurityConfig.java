package com.fluxo.infra.config;
import org.springframework.http.HttpMethod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desativa CSRF pois APIs REST com JWT não precisam disso
            .csrf(csrf -> csrf.disable()) 
            // Configura a aplicação para ser Stateless (não guarda sessão)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Regras de rotas
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Força a liberação apenas para POST
    .requestMatchers("/error").permitAll() // Libera a rota de erro para podermos ver logs reais
    .anyRequest().authenticated()
);
        
        return http.build();
    }
}