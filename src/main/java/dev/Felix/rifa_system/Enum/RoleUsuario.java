package dev.Felix.rifa_system.Enum;

public enum RoleUsuario {

    ADMIN("Administrador - Gerencia plataforma"),
    VENDEDOR("Vendedor - Cria e gerencia rifas"),
    CLIENTE("Cliente - Compra números");

    private final String descricao;

    RoleUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna authority para Spring Security
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
