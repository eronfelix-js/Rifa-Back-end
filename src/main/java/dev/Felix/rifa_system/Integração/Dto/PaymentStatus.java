package dev.Felix.rifa_system.Integração.Dto;


public enum PaymentStatus {
    PENDING("pending"),
    APPROVED("approved"),
    AUTHORIZED("authorized"),
    IN_PROCESS("in_process"),
    IN_MEDIATION("in_mediation"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    CHARGED_BACK("charged_back");

    private final String value;
    PaymentStatus(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static PaymentStatus fromString(String status) {
        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.value.equalsIgnoreCase(status)) {
                return ps;
            }
        }
        throw new IllegalArgumentException("Status desconhecido: " + status);
    }
    public boolean isFinal() {
        return this == APPROVED ||
                this == REJECTED ||
                this == CANCELLED ||
                this == REFUNDED ||
                this == CHARGED_BACK;
    }
    public boolean isSuccess() {
        return this == APPROVED || this == AUTHORIZED;
    }
}