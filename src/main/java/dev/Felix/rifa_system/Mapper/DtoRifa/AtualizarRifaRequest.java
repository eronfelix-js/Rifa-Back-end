package dev.Felix.rifa_system.Mapper.DtoRifa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para atualizar dados da rifa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarRifaRequest {

    private String titulo;
    private String descricao;
    private String imagemUrl;
}