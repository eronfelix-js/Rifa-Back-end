package dev.Felix.rifa_system.Mapper;

import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Mapper.DtoPagamento.PagamentoPixResponse;
import dev.Felix.rifa_system.Mapper.DtoPagamento.PagamentoResponse;
import org.springframework.stereotype.Component;

@Component
public class PagamentoMapper {

    /**
     * Converte Entity para Response PIX (com QR Code)
     */
    public PagamentoPixResponse toPixResponse(Pagamento pagamento) {
        return PagamentoPixResponse.builder()
                .id(pagamento.getId())
                .compraId(pagamento.getCompraId())
                .gateway(pagamento.getGateway())
                .referenceId(pagamento.getReferenceId())
                .qrCode(pagamento.getQrCode())
                .qrCodePayload(pagamento.getQrCodePayload())
                .valor(pagamento.getValor())
                .status(pagamento.getStatus())
                .dataExpiracao(pagamento.getDataExpiracao())
                .dataCriacao(pagamento.getDataCriacao())
                .build();
    }

    /**
     * Converte Entity para Response completa
     */
    public PagamentoResponse toResponse(Pagamento pagamento) {
        return PagamentoResponse.builder()
                .id(pagamento.getId())
                .compraId(pagamento.getCompraId())
                .gateway(pagamento.getGateway())
                .referenceId(pagamento.getReferenceId())
                .authorizationId(pagamento.getAuthorizationId())
                .valor(pagamento.getValor())
                .status(pagamento.getStatus())
                .dataExpiracao(pagamento.getDataExpiracao())
                .dataPagamento(pagamento.getDataPagamento())
                .dataCriacao(pagamento.getDataCriacao())
                .dataAtualizacao(pagamento.getDataAtualizacao())
                .build();
    }
}
