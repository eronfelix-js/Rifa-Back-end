package dev.Felix.rifa_system.Exceptions;

/**
 * Lançada quando recurso não é encontrado
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException rifa(String id) {
        return new ResourceNotFoundException("Rifa não encontrada: " + id);
    }

    public static ResourceNotFoundException usuario(String id) {
        return new ResourceNotFoundException("Usuário não encontrado: " + id);
    }

    public static ResourceNotFoundException compra(String id) {
        return new ResourceNotFoundException("Compra não encontrada: " + id);
    }
}