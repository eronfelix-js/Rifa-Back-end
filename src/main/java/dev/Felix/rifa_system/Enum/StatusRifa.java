package dev.Felix.rifa_system.Enum;

public enum StatusRifa {
    ATIVA("Ativa - Aceitando compras"),
    COMPLETA("Completa - Todos números vendidos"),
    SORTEADA("Sorteada - Já tem vencedor"),
    CANCELADA("Cancelada - Não teve compras");

    private final String descricao;

    StatusRifa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}