//package dev.Felix.rifa_system.Integração;
//
//
//import dev.Felix.rifa_system.Exceptions.PaymentException;
//import dev.Felix.rifa_system.Integração.Dto.PicPayPixRequest;
//import dev.Felix.rifa_system.Integração.Dto.PicPayPixResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
///**
// * Service de integração com PicPay
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PicPayService {
//
//    private final RestTemplate restTemplate;
//
//    // @Value("${app.picpay.api-url}")
//    // private String apiUrl;
//
//    // @Value("${app.picpay.token}")
//    // private String token;
//
//// @Value("${app.picpay.seller-token}")
//// private String sellerToken;
//
//// @Value("${app.picpay.callback-url}")
//// private String callbackUrl
//
//
//    /**
//     * Gerar QR Code PIX
//     */
//    public PicPayPixResponse gerarQrCodePix(UUID compraId, BigDecimal valor, String compradorNome, String compradorEmail) {
//        log.info("Gerando QR Code PIX no PicPay - Compra: {} - Valor: {}", compraId, valor);
//
//        try {
//            // Montar request
//            PicPayPixRequest request = PicPayPixRequest.builder()
//                    .referenceId(compraId.toString())
//                    .callbackUrl(callbackUrl)
//                    .value(valor)
//                    .buyer(PicPayPixRequest.Buyer.builder()
//                            .firstName(extrairPrimeiroNome(compradorNome))
//                            .lastName(extrairUltimoNome(compradorNome))
//                            .email(compradorEmail)
//                            .build())
//                    .build();
//
//            // Headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("x-picpay-token", token);
//
//            HttpEntity<PicPayPixRequest> entity = new HttpEntity<>(request, headers);
//
//            // Chamar API
//            String url = apiUrl + "/payments";
//            ResponseEntity<PicPayPixResponse> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    PicPayPixResponse.class
//            );
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                log.info("✅ QR Code PIX gerado com sucesso");
//                return response.getBody();
//            } else {
//                throw new PaymentException("Falha ao gerar QR Code PIX");
//            }
//
//        } catch (RestClientException e) {
//            log.error("❌ Erro ao chamar API PicPay: {}", e.getMessage(), e);
//            throw new PaymentException("Erro ao comunicar com PicPay", e);
//        }
//    }
//
//    /**
//     * Consultar status de pagamento
//     */
//    public String consultarStatus(String referenceId) {
//        log.info("Consultando status do pagamento: {}", referenceId);
//
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("x-picpay-token", token);
//
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//
//            String url = apiUrl + "/payments/" + referenceId;
//            ResponseEntity<PicPayPixResponse> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    entity,
//                    PicPayPixResponse.class
//            );
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody().getStatus();
//            }
//
//            return "UNKNOWN";
//
//        } catch (RestClientException e) {
//            log.error("Erro ao consultar status: {}", e.getMessage());
//            return "ERROR";
//        }
//    }
//
//    /**
//     * Validar webhook
//     */
//    public boolean validarWebhook(String receivedToken) {
//        return sellerToken.equals(receivedToken);
//    }
//
//    // Métodos auxiliares
//    private String extrairPrimeiroNome(String nomeCompleto) {
//        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
//            return "Cliente";
//        }
//        String[] partes = nomeCompleto.trim().split(" ");
//        return partes[0];
//    }
//
//    private String extrairUltimoNome(String nomeCompleto) {
//        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
//            return "Rifa";
//        }
//        String[] partes = nomeCompleto.trim().split(" ");
//        return partes.length > 1 ? partes[partes.length - 1] : "Rifa";
//    }
//}
//
//
//
