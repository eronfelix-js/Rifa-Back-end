package dev.Felix.rifa_system.Integração.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.Felix.rifa_system.Integração.Dto.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response completo ao consultar detalhes de um pagamento
 * Usado após receber webhook para verificar status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_detail")
    private String statusDetail;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("external_reference")
    private String externalReference;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;

    @JsonProperty("payment_type_id")
    private String paymentTypeId;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @JsonProperty("date_approved")
    private LocalDateTime dateApproved;

    @JsonProperty("date_last_updated")
    private LocalDateTime dateLastUpdated;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    @JsonProperty("transaction_details")
    private TransactionDetails transactionDetails;

    @JsonProperty("payer")
    private Payer payer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetails {

        @JsonProperty("net_received_amount")
        private BigDecimal netReceivedAmount;

        @JsonProperty("total_paid_amount")
        private BigDecimal totalPaidAmount;

        @JsonProperty("overpaid_amount")
        private BigDecimal overpaidAmount;

        @JsonProperty("external_resource_url")
        private String externalResourceUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
    }

    public boolean isApproved() {
        return "approved".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }

    public PaymentStatus getPaymentStatus() {
        return PaymentStatus.fromString(status);
    }
}