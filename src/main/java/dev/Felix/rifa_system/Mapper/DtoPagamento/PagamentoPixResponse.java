package dev.Felix.rifa_system.Mapper.DtoPagamento;

import dev.Felix.rifa_system.Enum.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response de Pagamento PIX
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoPixResponse {

    private UUID id;
    private UUID compraId;
    private String gateway;
    private String referenceId;
    private String qrCode; // Base64 da imagem
    private String qrCodePayload; // PIX copia e cola
    private BigDecimal valor;
    private StatusPagamento status;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataCriacao;
}
