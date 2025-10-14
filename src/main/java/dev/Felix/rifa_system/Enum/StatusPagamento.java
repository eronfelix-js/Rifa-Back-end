package dev.Felix.rifa_system.Enum;

public enum StatusPagamento {
    AGUARDANDO("Aguardando - QR Code gerado"),
    APROVADO("Aprovado - Pagamento confirmado"),
    RECUSADO("Recusado - Pagamento falhou"),
    EXPIRADO("Expirado - Tempo limite excedido");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}