package dev.Felix.rifa_system.Mapper.DtoUsuario;

import dev.Felix.rifa_system.Enum.RoleUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private UUID id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private RoleUsuario role;
    private Boolean ativo;
    private LocalDateTime dataCriacao;
}
