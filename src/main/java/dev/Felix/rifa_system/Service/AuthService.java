package dev.Felix.rifa_system.Service;


import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    /**
     * Autenticar usuário (Login)
     */
    public String autenticar(String email, String senha) {
        log.info("Tentando autenticar usuário: {}", email);

        try {
            // Tentar autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            // Buscar usuário
            Usuario usuario = usuarioService.buscarPorEmail(email);

            // Validar se está ativo
            if (!usuario.getAtivo()) {
                throw new UnauthorizedException("Usuário desativado");
            }

            String token = jwtService.gerarToken(usuario);

            log.info("Usuário autenticado com sucesso: {} - Role: {}", email, usuario.getRole());
            return token;

        } catch (BadCredentialsException e) {
            log.warn("Falha na autenticação: {}", email);
            throw new UnauthorizedException("Email ou senha inválidos");
        }
    }

    /**
     * Validar token JWT
     */
    public boolean validarToken(String token) {
        return jwtService.validarToken(token);
    }

    /**
     * Extrair email do token
     */
    public String extrairEmail(String token) {
        return jwtService.extrairEmail(token);
    }
}

