package dev.Felix.rifa_system.Security;


import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementação do UserDetailsService para Spring Security
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // Criar authority com role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRole().getAuthority());

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(Collections.singleton(authority))
                .accountExpired(false)
                .accountLocked(!usuario.getAtivo())
                .credentialsExpired(false)
                .disabled(!usuario.getAtivo())
                .build();
    }
}