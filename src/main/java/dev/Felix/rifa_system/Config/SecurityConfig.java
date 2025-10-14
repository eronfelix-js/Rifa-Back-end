package dev.Felix.rifa_system.Config;

import dev.Felix.rifa_system.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilitar CSRF (API REST com JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (não requerem autenticação)
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/webhook/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Rifas - Endpoints públicos (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/rifas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rifas/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rifas/*/numeros/disponiveis").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rifas/*/estatisticas").permitAll()

                        // Sorteios - Ver resultado (público)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sorteios/rifa/*").permitAll()

                        // Usuários - Buscar por ID (público)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/*").permitAll()

                        // Criar rifa - Apenas VENDEDOR ou ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/rifas")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        // Cancelar rifa - Apenas VENDEDOR ou ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/rifas/*")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        // Sortear - Apenas VENDEDOR ou ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/sorteios/**")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        // Todas as outras requisições precisam de autenticação
                        .anyRequest().authenticated()
                )

                // Stateless (sem sessão)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Adicionar filtro JWT antes do filtro de autenticação padrão
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configurar authentication provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}