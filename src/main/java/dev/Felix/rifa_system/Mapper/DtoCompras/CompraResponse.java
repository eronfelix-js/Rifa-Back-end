package dev.Felix.rifa_system.Mapper.DtoCompras;

import dev.Felix.rifa_system.Enum.StatusCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponse {

    private UUID id;
    private UUID rifaId;
    private String tituloRifa;
    private UUID compradorId;
    private String nomeComprador;
    private StatusCompra status;
    private BigDecimal valorTotal;
    private Integer quantidadeNumeros;
    private List<Integer> numeros;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}