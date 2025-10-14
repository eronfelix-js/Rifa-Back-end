package dev.Felix.rifa_system.Mapper.DtoCompras;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response simplificada para listagem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResumoResponse {

    private UUID id;
    private String tituloRifa;
    private Integer quantidadeNumeros;
    private BigDecimal valorTotal;
    private String status;
    private String dataCriacao;
}