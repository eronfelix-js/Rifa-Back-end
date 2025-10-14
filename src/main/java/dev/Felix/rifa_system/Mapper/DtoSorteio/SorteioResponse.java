package dev.Felix.rifa_system.Mapper.DtoSorteio;

import dev.Felix.rifa_system.Enum.MetodoSorteio;
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
public class SorteioResponse {

    private UUID id;
    private UUID rifaId;
    private String tituloRifa;
    private Integer numeroSorteado;
    private UUID compradorVencedorId;
    private String nomeVencedor;
    private String emailVencedor;
    private MetodoSorteio metodo;
    private String hashVerificacao;
    private LocalDateTime dataSorteio;
    private String observacoes;
}