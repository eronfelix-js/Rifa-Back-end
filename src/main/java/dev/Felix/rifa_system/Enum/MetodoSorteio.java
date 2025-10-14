package dev.Felix.rifa_system.Enum;

public enum MetodoSorteio {
    AUTOMATICO("Automático - Sistema sorteia"),
    MANUAL("Manual - Vendedor sorteia"),
    LOTERIA_FEDERAL("Loteria Federal - Baseado em resultado oficial");

    private final String descricao;

    MetodoSorteio(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}