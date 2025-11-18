package dev.Felix.rifa_system.Integração.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixResponse {

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

    @JsonProperty("point_of_interaction")
    private PointOfInteraction pointOfInteraction;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @JsonProperty("date_of_expiration")
    private LocalDateTime dateOfExpiration;

    @JsonProperty("date_approved")
    private LocalDateTime dateApproved;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointOfInteraction {

        @JsonProperty("transaction_data")
        private TransactionData transactionData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {

        /** QR Code em Base64 (imagem)*/
        @JsonProperty("qr_code_base64")
        private String qrCodeBase64;

        @JsonProperty("qr_code")
        private String qrCode;

        @JsonProperty("ticket_url")
        private String ticketUrl;
    }


    public String getQrCodeBase64() {
        return pointOfInteraction != null &&
                pointOfInteraction.transactionData != null
                ? pointOfInteraction.transactionData.qrCodeBase64
                : null;
    }

    public String getQrCode() {
        return pointOfInteraction != null &&
                pointOfInteraction.transactionData != null
                ? pointOfInteraction.transactionData.qrCode
                : null;
    }

    public String getTicketUrl() {
        return pointOfInteraction != null &&
                pointOfInteraction.transactionData != null
                ? pointOfInteraction.transactionData.ticketUrl
                : null;
    }
}