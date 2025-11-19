package dev.Felix.rifa_system.Controller;

import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Mapper.DtoUsuario.LoginRequest;
import dev.Felix.rifa_system.Mapper.DtoUsuario.LoginResponse;
import dev.Felix.rifa_system.Mapper.DtoUsuario.RegistrarUsuarioRequest;
import dev.Felix.rifa_system.Mapper.DtoUsuario.UsuarioResponse;
import dev.Felix.rifa_system.Mapper.UsuarioMapper;
import dev.Felix.rifa_system.Service.AuthService;
import dev.Felix.rifa_system.Service.EmailService;
import dev.Felix.rifa_system.Service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        log.info("POST /api/v1/auth/register - Email: {}", request.getEmail());

        Usuario usuario = usuarioMapper.toEntity(request);
        Usuario usuarioSalvo = usuarioService.registrar(usuario);
        UsuarioResponse response = usuarioMapper.toResponse(usuarioSalvo);
        emailService.enviarEmailBoasVindas(usuarioSalvo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Email: {}", request.getEmail());

        String token = authService.autenticar(request.getEmail(), request.getSenha());
        Usuario usuario = usuarioService.buscarPorEmail(request.getEmail());
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);

        LoginResponse response = new LoginResponse(token, usuarioResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        log.info("GET /api/v1/auth/me");

        UUID usuarioId = UUID.fromString(authentication.getName());
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        UsuarioResponse response = usuarioMapper.toResponse(usuario);

        return ResponseEntity.ok(response);
    }
}