package dev.Felix.rifa_system.Controller;

import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Mapper.DtoUsuario.UsuarioResponse;
import dev.Felix.rifa_system.Mapper.UsuarioMapper;
import dev.Felix.rifa_system.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable UUID id) {
        log.info("GET /api/v1/usuarios/{}", id);

        Usuario usuario = usuarioService.buscarPorId(id);
        UsuarioResponse response = usuarioMapper.toResponse(usuario);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> atualizarPerfil(
            @RequestParam String nome,
            @RequestParam(required = false) String telefone,
            Authentication authentication
    ) {
        log.info("PUT /api/v1/usuarios/perfil");

        UUID usuarioId = UUID.fromString(authentication.getName());
        Usuario usuario = usuarioService.atualizarPerfil(usuarioId, nome, telefone);
        UsuarioResponse response = usuarioMapper.toResponse(usuario);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha,
            Authentication authentication
    ) {
        log.info("PUT /api/v1/usuarios/senha");

        UUID usuarioId = UUID.fromString(authentication.getName());
        usuarioService.alterarSenha(usuarioId, senhaAtual, novaSenha);

        return ResponseEntity.noContent().build();
    }
}