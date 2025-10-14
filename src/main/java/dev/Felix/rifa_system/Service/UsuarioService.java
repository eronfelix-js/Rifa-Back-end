package dev.Felix.rifa_system.Service;


import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Enum.RoleUsuario;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registrar novo usuário
     */
    @Transactional
    public Usuario registrar(Usuario usuario) {
        // Validar email único
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        // Validar CPF único
        if (usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        // Criptografar senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        // Definir role padrão se não informado
        if (usuario.getRole() == null) {
            usuario.setRole(RoleUsuario.CLIENTE);
        }

        // Ativar usuário
        usuario.setAtivo(true);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioSalvo;
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.usuario(id.toString()));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional
    public Usuario atualizarPerfil(UUID id, String nome, String telefone) {
        Usuario usuario = buscarPorId(id);
        usuario.setNome(nome);
        usuario.setTelefone(telefone);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenha(UUID id, String senhaAtual, String novaSenha) {
        log.info("Alterando senha do usuário: {}", id);
        Usuario usuario = buscarPorId(id);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        // Atualizar senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        log.info("Senha alterada com sucesso");
    }

    @Transactional
    public void desativar(UUID id) {

        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void validarVendedor(UUID usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        if (!usuario.isVendedor() && !usuario.isAdmin()) {
            throw new BusinessException("Apenas vendedores podem criar rifas");
        }
    }
}