package dev.Felix.rifa_system.Integração.DtoWebHookPicPay;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPicPayRequest {

    @NotBlank(message = "Reference ID é obrigatório")
    private String referenceId;

    @NotBlank(message = "Status é obrigatório")
    private String status;

    private String authorizationId;
}