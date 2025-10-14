package dev.Felix.rifa_system.Exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException acessoNegado() {
        return new UnauthorizedException("Acesso negado");
    }

    public static UnauthorizedException tokenInvalido() {
        return new UnauthorizedException("Token inválido ou expirado");
    }
}
