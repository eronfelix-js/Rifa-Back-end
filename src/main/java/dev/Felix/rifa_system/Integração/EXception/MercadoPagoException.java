package dev.Felix.rifa_system.Integração.EXception;

/**
 * Exception customizada para erros de integração com Mercado Pago
 */
public class MercadoPagoException extends RuntimeException {

    private String errorCode;
    private Integer httpStatus;

    public MercadoPagoException(String message) {
        super(message);
    }

    public MercadoPagoException(String message, Throwable cause) {
        super(message, cause);
    }

    public MercadoPagoException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MercadoPagoException(String message, String errorCode, Integer httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * Factory methods para casos comuns
     */
    public static MercadoPagoException apiError(String message, Integer status) {
        return new MercadoPagoException(
                "Erro na API do Mercado Pago: " + message,
                "API_ERROR",
                status
        );
    }

    public static MercadoPagoException timeout() {
        return new MercadoPagoException(
                "Timeout ao comunicar com Mercado Pago",
                "TIMEOUT"
        );
    }

    public static MercadoPagoException invalidResponse(String message) {
        return new MercadoPagoException(
                "Response inválido do Mercado Pago: " + message,
                "INVALID_RESPONSE"
        );
    }

    public static MercadoPagoException configurationError(String message) {
        return new MercadoPagoException(
                "Erro de configuração: " + message,
                "CONFIG_ERROR"
        );
    }
}