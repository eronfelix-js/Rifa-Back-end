package dev.Felix.rifa_system.Integração.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookNotification {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("live_mode")
    private Boolean liveMode;

    @JsonProperty("type")
    private String type;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @JsonProperty("application_id")
    private Long applicationId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("action")
    private String action;

    @JsonProperty("data")
    private ResourceData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceData {
        @JsonProperty("id")
        private String id;
    }
    public boolean isPaymentNotification() {
        return "payment".equalsIgnoreCase(type);
    }

    /**
     * Verifica se é atualização de pagamento
     */
    public boolean isPaymentUpdate() {
        return "payment.updated".equalsIgnoreCase(action);
    }

    /**
     * Obtém o ID do pagamento
     */
    public Long getPaymentId() {
        if (data != null && data.id != null) {
            try {
                return Long.parseLong(data.id);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}