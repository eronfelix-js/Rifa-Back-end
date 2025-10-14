package dev.Felix.rifa_system.Integração.DtoPicPay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PicPayPixRequest {

    private String referenceId;
    private String callbackUrl;
    private BigDecimal value;
    private Buyer buyer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Buyer {
        private String firstName;
        private String lastName;
        private String email;
    }
}