package dev.Felix.rifa_system.Mapper;

import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Mapper.DtoUsuario.RegistrarUsuarioRequest;
import dev.Felix.rifa_system.Mapper.DtoUsuario.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(RegistrarUsuarioRequest request) {
        return Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .telefone(request.getTelefone())
                .senha(request.getSenha()) // Será criptografada no service
                .role(request.getRole())
                .build();
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .role(usuario.getRole())
                .ativo(usuario.getAtivo())
                .dataCriacao(usuario.getDataCriacao())
                .build();
    }
}