package dev.Felix.rifa_system.Integração.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixRequest {

    /**
     * Valor da transação
     * Obrigatório
     */
    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    /**
     * Descrição do pagamento
     * Obrigatório
     */
    @JsonProperty("description")
    private String description;

    /**
     * Método de pagamento (sempre "pix")
     * Obrigatório
     */
    @JsonProperty("payment_method_id")
    @Builder.Default
    private String paymentMethodId = "pix";

    /**
     * Dados do pagador
     * Obrigatório
     */
    @JsonProperty("payer")
    private Payer payer;

    /**
     * URL de notificação (webhook)
     * Opcional
     */
    @JsonProperty("notification_url")
    private String notificationUrl;

    /**
     * Referência externa (ID da compra no nosso sistema)
     * Opcional mas MUITO recomendado
     */
    @JsonProperty("external_reference")
    private String externalReference;

    /**
     * Data de expiração do PIX
     * Opcional (padrão: 24h)
     */
    @JsonProperty("date_of_expiration")
    private LocalDateTime dateOfExpiration;

    /**
     * Dados do pagador
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {
        @JsonProperty("email")
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("identification")
        private Identification identification;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Identification {

        @JsonProperty("type")
        private String type;
        @JsonProperty("number")
        private String number;
    }
}