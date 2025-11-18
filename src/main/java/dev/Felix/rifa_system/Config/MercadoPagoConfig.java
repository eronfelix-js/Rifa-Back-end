package dev.Felix.rifa_system.Config;

import dev.Felix.rifa_system.Integração.EXception.MercadoPagoException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
@Slf4j
@Getter
public class MercadoPagoConfig {

    @Value("${app.mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.mercadopago.public-key:}")
    private String publicKey;

    @Value("${app.mercadopago.webhook-secret:}")
    private String webhookSecret;

    private String apiUrl= "https://api.mercadopago.com/v1/payments";

    @Value("${app.mercadopago.notification-url:}")
    private String notificationUrl;

    @Value("${app.pagamento.pix.expiracao-minutos:15}")
    private Integer pixExpiracaoMinutos;

    @Value("${app.pagamento.pix.taxa-percentual:0.99}")
    private Double pixTaxaPercentual;

    @Bean(name = "mercadoPagoRestTemplate")
    public RestTemplate mercadoPagoRestTemplate(RestTemplateBuilder builder) {
        log.info("Configurando RestTemplate para Mercado Pago");
        validateConfiguration();

        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return builder
                .rootUri(apiUrl)
                .requestFactory(() -> factory)
                .additionalInterceptors(mercadoPagoInterceptor())
                .build();
    }

    /**
     * Interceptor para adicionar headers em todas as requisições
     */
    private ClientHttpRequestInterceptor mercadoPagoInterceptor() {
        return (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();

            // Authorization header
            headers.setBearerAuth(accessToken);

            // Content-Type
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Accept
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            // User-Agent customizado
            headers.set("User-Agent", "RifaSystem/1.0");

            // X-Idempotency-Key para evitar duplicação
            String idempotencyKey = java.util.UUID.randomUUID().toString();
            headers.set("X-Idempotency-Key", idempotencyKey);

            log.debug("Requisição MP: {} {}", request.getMethod(), request.getURI());

            return execution.execute(request, body);
        };
    }

    /**
     * Validar configuração obrigatória
     */
    private void validateConfiguration() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("❌ Access Token do Mercado Pago não configurado!");
            log.error("Configure: app.mercadopago.access-token em application.properties");
            throw MercadoPagoException.configurationError("Access Token não configurado");
        }

        if (!accessToken.startsWith("APP_USR-") && !accessToken.startsWith("TEST-")) {
            log.warn("⚠️ Access Token parece inválido. Deve começar com 'APP_USR-' ou 'TEST-'");
        }

        if (notificationUrl == null || notificationUrl.trim().isEmpty()) {
            log.warn("⚠️ Notification URL não configurada. Webhook não funcionará!");
        }

        log.info("✅ Configuração Mercado Pago validada");
        log.info("📍 API URL: {}", apiUrl);
        log.info("📍 Notification URL: {}", notificationUrl);
        log.info("⏱️ Expiração PIX: {} minutos", pixExpiracaoMinutos);
        log.info("💰 Taxa PIX: {}%", pixTaxaPercentual);
    }

    /**
     * Obter taxa PIX como BigDecimal
     */
    public java.math.BigDecimal getTaxaPix() {
        return java.math.BigDecimal.valueOf(pixTaxaPercentual / 100);
    }
}