package dev.Felix.rifa_system.Integração.MercadoPago;

import dev.Felix.rifa_system.Config.MercadoPagoConfig;
import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Integração.Dto.PixRequest;
import dev.Felix.rifa_system.Integração.Dto.PixResponse;
import dev.Felix.rifa_system.Integração.EXception.MercadoPagoException;
import dev.Felix.rifa_system.Integração.Dto.PaymentDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service principal para integração com Mercado Pago
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    private final MercadoPagoClient client;
    private final MercadoPagoConfig config;

    public PixResponse criarPagamentoPix(Compra compra, Usuario comprador) {
        log.info("💰 Criando pagamento PIX - Compra: {} - Valor: {}",
                compra.getId(), compra.getValorTotal());

        try {
            PixRequest request = montarRequest(compra, comprador);
            PixResponse response = client.criarPagamentoPix(request);
            validarResponse(response);
            log.info("✅ PIX criado com sucesso - Payment ID: {}", response.getId());
            return response;
        } catch (MercadoPagoException e) {
            log.error("❌ Erro ao criar PIX: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erro inesperado ao criar PIX", e);
            throw new MercadoPagoException("Erro ao criar pagamento PIX", e);
        }
    }

    /**
     * Consultar status de um pagamento
     * Usado pelo webhook para verificar se pagamento foi aprovado
     *
     * @param paymentId ID do pagamento no Mercado Pago
     * @return Detalhes completos do pagamento
     */
    public PaymentDetailsResponse consultarPagamento(Long paymentId) {
        log.info("🔍 Consultando status do pagamento: {}", paymentId);

        try {
            PaymentDetailsResponse response = client.consultarPagamento(paymentId);

            log.info("Status: {} - External Reference: {}",
                    response.getStatus(), response.getExternalReference());

            return response;

        } catch (MercadoPagoException e) {
            log.error("❌ Erro ao consultar pagamento: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Montar request para criar PIX
     */
    private PixRequest montarRequest(Compra compra, Usuario comprador) {
        // Calcular data de expiração
        LocalDateTime dataExpiracao = LocalDateTime.now()
                .plusMinutes(config.getPixExpiracaoMinutos());

        // Montar descrição
        String descricao = String.format(
                "Rifa - %d números - Compra #%s",
                compra.getQuantidadeNumeros(),
                compra.getId().toString().substring(0, 8)
        );

        // Separar nome
        String[] nomePartes = separarNome(comprador.getNome());

        // Criar identificação (CPF)
        PixRequest.Identification identification = null;
        if (comprador.getCpf() != null && !comprador.getCpf().isEmpty()) {
            identification = PixRequest.Identification.builder()
                    .type("CPF")
                    .number(comprador.getCpf().replaceAll("[^0-9]", ""))
                    .build();
        }

        // Criar payer
        PixRequest.Payer payer = PixRequest.Payer.builder()
                .email(comprador.getEmail())
                .firstName(nomePartes[0])
                .lastName(nomePartes[1])
                .identification(identification)
                .build();

        // Montar request completo
        return PixRequest.builder()
                .transactionAmount(compra.getValorTotal())
                .description(descricao)
                .paymentMethodId("pix")
                .payer(payer)
                .notificationUrl(config.getNotificationUrl())
                .externalReference(compra.getId().toString())
                .dateOfExpiration(dataExpiracao)
                .build();
    }

    /**
     * Validar response do Mercado Pago
     */
    private void validarResponse(PixResponse response) {
        if (response.getId() == null) {
            throw MercadoPagoException.invalidResponse("Payment ID ausente");
        }

        if (response.getQrCode() == null || response.getQrCode().isEmpty()) {
            throw MercadoPagoException.invalidResponse("QR Code ausente");
        }

        if (!"pending".equalsIgnoreCase(response.getStatus())) {
            log.warn("⚠️ Status inesperado ao criar PIX: {}", response.getStatus());
        }
    }

    /**
     * Separar primeiro nome e sobrenome
     */
    private String[] separarNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            return new String[]{"Cliente", "Rifa"};
        }

        String[] partes = nomeCompleto.trim().split(" ", 2);

        if (partes.length == 1) {
            return new String[]{partes[0], "Rifa"};
        }

        return partes;
    }

    /**
     * Extrair UUID da referência externa
     */
    public UUID extrairCompraId(String externalReference) {
        try {
            return UUID.fromString(externalReference);
        } catch (Exception e) {
            log.error("❌ External reference inválido: {}", externalReference);
            throw new MercadoPagoException("External reference inválido: " + externalReference);
        }
    }

    /**
     * Verificar se API está acessível
     */
    public boolean verificarConexao() {
        return client.verificarConexao();
    }
}