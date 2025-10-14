package dev.Felix.rifa_system.Mapper.DtoRifa;

import dev.Felix.rifa_system.Enum.StatusRifa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RifaResponse {

    private UUID id;
    private UUID usuarioId;
    private String nomeVendedor;
    private String emailVendedor;
    private String titulo;
    private String descricao;
    private String imagemUrl;
    private Integer quantidadeNumeros;
    private BigDecimal precoPorNumero;
    private BigDecimal valorTotal;
    private StatusRifa status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataLimite;
    private LocalDateTime dataSorteio;
    private Integer numeroVencedor;
    private UUID compradorVencedorId;
    private String nomeVencedor;
    private Boolean sorteioAutomatico;
    private Boolean sortearAoVenderTudo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}