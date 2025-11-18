package dev.Felix.rifa_system.Integração;

import dev.Felix.rifa_system.Integração.Dto.PaymentDetailsResponse;
import dev.Felix.rifa_system.Integração.Dto.WebhookNotification;
import dev.Felix.rifa_system.Integração.MercadoPago.MercadoPagoService;
import dev.Felix.rifa_system.Service.PagamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PagamentoService pagamentoService;
    private final MercadoPagoService mercadoPagoService;

    @Value("${app.mercadopago.webhook-secret:}")
    private String mercadoPagoWebhookSecret;

    /**
     * ✅ Webhook do Mercado Pago
     * Recebe notificações quando um pagamento muda de status
     *
     * Documentação: https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks
     */
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> webhookMercadoPago(
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestBody WebhookNotification notification
    ) {
        log.info("📨 Webhook Mercado Pago recebido");
        log.info("Type: {} | Action: {} | Payment ID: {}",
                notification.getType(),
                notification.getAction(),
                notification.getPaymentId()
        );

        try {
            // 1️⃣ Validar assinatura (segurança)
            if (!validarAssinaturaMercadoPago(signature, requestId, notification)) {
                log.warn("❌ Webhook rejeitado - Assinatura inválida");
                return ResponseEntity.status(401).build();
            }

            // 2️⃣ Filtrar apenas notificações de pagamento
            if (!notification.isPaymentNotification()) {
                log.info("ℹ️ Notificação ignorada - Tipo: {}", notification.getType());
                return ResponseEntity.ok().build();
            }

            // 3️⃣ Obter ID do pagamento
            Long paymentId = notification.getPaymentId();
            if (paymentId == null) {
                log.warn("⚠️ Payment ID ausente na notificação");
                return ResponseEntity.badRequest().build();
            }

            // 4️⃣ Consultar detalhes do pagamento no Mercado Pago
            log.info("🔍 Consultando pagamento {} no Mercado Pago", paymentId);
            PaymentDetailsResponse paymentDetails = mercadoPagoService.consultarPagamento(paymentId);

            // 5️⃣ Extrair compraId do external_reference
            String externalReference = paymentDetails.getExternalReference();
            if (externalReference == null || externalReference.isEmpty()) {
                log.error("❌ External reference ausente no pagamento {}", paymentId);
                return ResponseEntity.ok().build(); // Retornar 200 para não retentar
            }

            UUID compraId = UUID.fromString(externalReference);
            log.info("📦 CompraId extraído: {}", compraId);

            // 6️⃣ Processar baseado no status
            String status = paymentDetails.getStatus();
            log.info("Status do pagamento: {}", status);

            switch (status.toLowerCase()) {
                case "approved":
                    log.info("✅ Pagamento aprovado - Processando...");
                    pagamentoService.aprovarPagamentoPorCompraId(
                            compraId,
                            paymentDetails.getAuthorizationCode()
                    );
                    log.info("✅ Pagamento processado com sucesso");
                    break;

                case "rejected":
                case "cancelled":
                case "refunded":
                    log.info("❌ Pagamento recusado/cancelado - Liberando números");
                    pagamentoService.recusarPagamentoPorCompraId(compraId);
                    break;

                case "pending":
                case "in_process":
                    log.info("⏳ Pagamento pendente - Aguardando confirmação");
                    break;

                default:
                    log.warn("⚠️ Status desconhecido: {}", status);
            }

            // ✅ SEMPRE retornar 200 OK para evitar reenvio do webhook
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook Mercado Pago", e);

            // Retornar 200 para evitar retry infinito
            // (erro já foi logado para investigação)
            return ResponseEntity.ok().build();
        }
    }

    private boolean validarAssinaturaMercadoPago(
            String signature,
            String requestId,
            WebhookNotification notification
    ) {
        // Se não tiver secret configurado, apenas loga warning
        if (mercadoPagoWebhookSecret == null || mercadoPagoWebhookSecret.isEmpty()) {
            log.warn("⚠️ Webhook secret não configurado - Validação de assinatura desabilitada");
            return true; // Permitir em desenvolvimento
        }

        if (signature == null || signature.isEmpty()) {
            log.warn("⚠️ Signature ausente no header");
            return false;
        }

        try {
            // Extrair partes da assinatura
            // Formato: "ts=1234567890,v1=hash_aqui"
            String[] parts = signature.split(",");
            String timestamp = null;
            String hash = null;

            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0])) {
                        timestamp = keyValue[1];
                    } else if ("v1".equals(keyValue[0])) {
                        hash = keyValue[1];
                    }
                }
            }

            if (timestamp == null || hash == null) {
                log.warn("⚠️ Formato de assinatura inválido");
                return false;
            }

            // Construir payload para validação
            // Formato: id={data.id}&request-id={x-request-id}&ts={timestamp}
            String payload = String.format(
                    "id=%s&request-id=%s&ts=%s",
                    notification.getData().getId(),
                    requestId != null ? requestId : "",
                    timestamp
            );

            // Calcular HMAC-SHA256
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    mercadoPagoWebhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Converter para hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String calculatedHash = hexString.toString();

            // Comparar hashes (time-constant comparison)
            boolean valid = java.security.MessageDigest.isEqual(
                    calculatedHash.getBytes(StandardCharsets.UTF_8),
                    hash.getBytes(StandardCharsets.UTF_8)
            );

            if (!valid) {
                log.warn("❌ Hash inválido - Esperado: {} | Recebido: {}", calculatedHash, hash);
            }

            return valid;

        } catch (Exception e) {
            log.error("❌ Erro ao validar assinatura", e);
            return false;
        }
    }

    /**
     * ✅ Endpoint de teste para webhook Mercado Pago (DEV)
     * ⚠️ REMOVER EM PRODUÇÃO!
     */
    @PostMapping("/mercadopago/test")
    public ResponseEntity<String> testarWebhookMercadoPago(
            @RequestBody WebhookNotification notification
    ) {
        log.info("🧪 Teste de webhook Mercado Pago - Payment ID: {}", notification.getPaymentId());

        try {
            Long paymentId = notification.getPaymentId();
            if (paymentId == null) {
                return ResponseEntity.badRequest().body("Payment ID ausente");
            }

            PaymentDetailsResponse details = mercadoPagoService.consultarPagamento(paymentId);
            UUID compraId = UUID.fromString(details.getExternalReference());

            pagamentoService.aprovarPagamentoPorCompraId(
                    compraId,
                    details.getAuthorizationCode()
            );

            return ResponseEntity.ok("Webhook processado com sucesso - Compra: " + compraId);

        } catch (Exception e) {
            log.error("Erro no teste: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}