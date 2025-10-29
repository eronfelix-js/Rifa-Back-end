package dev.Felix.rifa_system.Enum;

public enum StatusCompra {
    PENDENTE("Pendente - Aguardando pagamento"),
    PAGO("Pago - Pagamento confirmado"),
    CONFIRMADO("Confirmado - Compra confirmada"),
    EXPIRADO("Expirado - Não pagou no prazo"),
    CANCELADO("Cancelado - Cancelado pelo usuário ou sistema");

    private final String descricao;

    StatusCompra(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}