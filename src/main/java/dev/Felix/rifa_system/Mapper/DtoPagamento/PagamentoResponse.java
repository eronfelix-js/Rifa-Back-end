package dev.Felix.rifa_system.Mapper.DtoPagamento;

import dev.Felix.rifa_system.Enum.StatusPagamento;
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
public class PagamentoResponse {

    private UUID id;
    private UUID compraId;
    private String gateway;
    private String referenceId;
    private String authorizationId;
    private BigDecimal valor;
    private StatusPagamento status;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataPagamento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}