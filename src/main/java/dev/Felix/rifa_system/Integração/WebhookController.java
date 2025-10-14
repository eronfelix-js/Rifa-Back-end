package dev.Felix.rifa_system.Integração;

import dev.Felix.rifa_system.Integração.DtoWebHookPicPay.WebhookPicPayRequest;
import dev.Felix.rifa_system.Service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PagamentoService pagamentoService;

    @Value("${app.picpay.seller-token:seu-seller-token-aqui}")
    private String sellerToken;

    /**
     * Webhook do PicPay
     * Recebe notificações quando um pagamento é confirmado
     */
    @PostMapping("/picpay")
    public ResponseEntity<Void> webhookPicPay(
            @RequestHeader(value = "x-seller-token", required = false) String receivedToken,
            @Valid @RequestBody WebhookPicPayRequest request
    ) {
        log.info("📨 Webhook PicPay recebido - Reference: {} - Status: {}",
                request.getReferenceId(), request.getStatus());

        try {
            // 1. Validar token de segurança
            if (receivedToken == null || !receivedToken.equals(sellerToken)) {
                log.warn("❌ Webhook rejeitado - Token inválido");
                return ResponseEntity.status(401).build();
            }

            // 2. Processar pagamento baseado no status
            switch (request.getStatus().toLowerCase()) {
                case "paid":
                case "completed":
                    log.info("✅ Pagamento aprovado");
                    pagamentoService.aprovarPagamento(
                            request.getReferenceId(),
                            request.getAuthorizationId()
                    );
                    break;

                case "refunded":
                case "cancelled":
                case "expired":
                    log.info("❌ Pagamento recusado/cancelado");
                    pagamentoService.recusarPagamento(request.getReferenceId());
                    break;

                default:
                    log.warn("⚠️ Status desconhecido: {}", request.getStatus());
            }

            // SEMPRE retornar 200 OK para evitar reenvio do webhook
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook: {}", e.getMessage(), e);
            // Mesmo com erro, retornar 200 para não retentar
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Endpoint para testar o webhook (DEV)
     */
    @PostMapping("/picpay/test")
    public ResponseEntity<String> testarWebhook(@Valid @RequestBody WebhookPicPayRequest request) {
        log.info("🧪 Teste de webhook - Reference: {}", request.getReferenceId());

        try {
            pagamentoService.aprovarPagamento(
                    request.getReferenceId(),
                    request.getAuthorizationId()
            );
            return ResponseEntity.ok("Webhook processado com sucesso");
        } catch (Exception e) {
            log.error("Erro no teste: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}