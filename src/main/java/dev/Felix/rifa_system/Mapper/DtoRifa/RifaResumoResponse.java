package dev.Felix.rifa_system.Mapper.DtoRifa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response simplificada para listagens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RifaResumoResponse {

    private UUID id;
    private String titulo;
    private String imagemUrl;
    private BigDecimal precoPorNumero;
    private Integer quantidadeNumeros;
    private Long numerosDisponiveis;
    private Long numerosVendidos;
    private BigDecimal percentualVendido;
    private String status;
    private String nomeVendedor;
    private LocalDateTime dataCriacao;
}