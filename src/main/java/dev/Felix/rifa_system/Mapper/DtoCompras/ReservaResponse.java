package dev.Felix.rifa_system.Mapper.DtoCompras;

import dev.Felix.rifa_system.Mapper.DtoPagamento.PagamentoPixResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response da reserva de números (inclui dados do pagamento)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {

    private UUID compraId;
    private UUID rifaId;
    private String tituloRifa;
    private Integer quantidadeNumeros;
    private List<Integer> numeros;
    private BigDecimal valorTotal;
    private LocalDateTime dataExpiracao;
    private Integer minutosParaExpirar;

    // Dados do pagamento PIX (pode ser null)
    private PagamentoPixResponse pagamento;
}