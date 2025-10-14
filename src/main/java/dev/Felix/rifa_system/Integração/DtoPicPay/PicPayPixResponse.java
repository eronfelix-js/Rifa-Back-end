package dev.Felix.rifa_system.Integração.DtoPicPay;

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
public class PicPayPixResponse {

    private String referenceId;
    private String authorizationId;
    private String status;
    private BigDecimal value;
    private String qrCode; // Base64
    private String paymentUrl;
    private LocalDateTime expiresAt;
}