package dev.Felix.rifa_system.Enum;

public enum StatusNumero {
    DISPONIVEL("Disponível para compra"),
    RESERVADO("Reservado - Aguardando pagamento"),
    VENDIDO("Vendido - Pagamento confirmado");

    private final String descricao;

    StatusNumero(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}