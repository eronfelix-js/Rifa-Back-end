package dev.Felix.rifa_system.Integração.MercadoPago;

import dev.Felix.rifa_system.Integração.Dto.PaymentDetailsResponse;
import dev.Felix.rifa_system.Integração.Dto.PixRequest;
import dev.Felix.rifa_system.Integração.Dto.PixResponse;
import dev.Felix.rifa_system.Integração.EXception.MercadoPagoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoClient {

    @Qualifier("mercadoPagoRestTemplate")
    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.mercadopago.com/v1/payments";
    private static final String PAYMENTS_ENDPOINT = "/v1/payments";


    public PixResponse criarPagamentoPix(PixRequest request) {
        log.info("📤 Criando pagamento PIX no Mercado Pago");
        log.debug("Request: {}", request);

        try {
            HttpEntity<PixRequest> entity = new HttpEntity<>(request);

            ResponseEntity<PixResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    PixResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                log.info("✅ Pagamento PIX criado com sucesso - ID: {}", response.getBody().getId());
                return response.getBody();
            } else {
                log.error("❌ Response inesperado: Status {} - Body: {}",
                        response.getStatusCode(), response.getBody());
                throw MercadoPagoException.invalidResponse(
                        "Status: " + response.getStatusCode()
                );
            }

        } catch (HttpClientErrorException e) {
            log.error("❌ Erro 4xx ao criar pagamento: {} - Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw MercadoPagoException.apiError(
                    "Erro do cliente: " + e.getMessage(),
                    e.getStatusCode().value()
            );

        } catch (HttpServerErrorException e) {
            log.error("❌ Erro 5xx do servidor Mercado Pago: {} - Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw MercadoPagoException.apiError(
                    "Erro do servidor: " + e.getMessage(),
                    e.getStatusCode().value()
            );

        } catch (ResourceAccessException e) {
            log.error("❌ Timeout ou erro de conexão: {}", e.getMessage());
            throw MercadoPagoException.timeout();

        } catch (Exception e) {
            log.error("❌ Erro inesperado ao criar pagamento PIX", e);
            throw new MercadoPagoException("Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    /**
     * Consultar detalhes de um pagamento
     * Usado após receber webhook para validar status
     *
     * @param paymentId ID do pagamento no Mercado Pago
     * @return Detalhes completos do pagamento
     * @throws MercadoPagoException em caso de erro
     */
    public PaymentDetailsResponse consultarPagamento(Long paymentId) {
        log.info("🔍 Consultando pagamento {} no Mercado Pago", paymentId);

        try {
            String url = PAYMENTS_ENDPOINT + "/" + paymentId;

            ResponseEntity<PaymentDetailsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    PaymentDetailsResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("✅ Pagamento consultado - Status: {}", response.getBody().getStatus());
                return response.getBody();
            } else {
                throw MercadoPagoException.invalidResponse(
                        "Status: " + response.getStatusCode()
                );
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Pagamento {} não encontrado", paymentId);
            throw MercadoPagoException.apiError("Pagamento não encontrado", 404);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ Erro ao consultar pagamento: {}", e.getMessage());
            throw MercadoPagoException.apiError(
                    e.getMessage(),
                    e.getStatusCode().value()
            );

        } catch (ResourceAccessException e) {
            log.error("❌ Timeout ao consultar pagamento: {}", e.getMessage());
            throw MercadoPagoException.timeout();

        } catch (Exception e) {
            log.error("❌ Erro inesperado ao consultar pagamento", e);
            throw new MercadoPagoException("Erro ao consultar pagamento: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar health da API do Mercado Pago
     * (Para testes e monitoramento)
     */
    public boolean verificarConexao() {
        try {
            log.debug("🔌 Verificando conexão com Mercado Pago");
            // Mercado Pago não tem endpoint de health oficial
            // Então vamos tentar consultar um pagamento inexistente
            // Se retornar 404, significa que API está no ar
            restTemplate.getForEntity(PAYMENTS_ENDPOINT + "/999999999", String.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            // 404 é esperado - API está funcionando
            log.debug("✅ API Mercado Pago está respondendo");
            return true;
        } catch (Exception e) {
            log.warn("⚠️ Erro ao verificar conexão com Mercado Pago: {}", e.getMessage());
            return false;
        }
    }
}