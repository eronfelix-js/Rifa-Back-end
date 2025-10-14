package dev.Felix.rifa_system.Security;

import dev.Felix.rifa_system.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro que intercepta todas as requisições e valida o JWT
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extrair header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Se não tem header ou não começa com "Bearer ", pular
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extrair token (remover "Bearer ")
            final String jwt = authHeader.substring(7);

            // 3. Extrair email do token
            final String userEmail = jwtService.extrairEmail(jwt);

            // 4. Se tem email e usuário ainda não está autenticado
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Carregar detalhes do usuário
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 6. Validar token
                if (jwtService.validarToken(jwt)) {

                    // 7. Extrair role do token
                    String role = jwtService.extrairRole(jwt);
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                    // 8. Criar authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            jwtService.extrairUsuarioId(jwt), // Principal = ID do usuário
                            null,
                            Collections.singleton(authority)
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. Definir authentication no contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuário autenticado: {} - Role: {}", userEmail, role);
                }
            }

        } catch (Exception e) {
            log.error("Erro ao processar JWT: {}", e.getMessage());
        }

        // Continuar a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
