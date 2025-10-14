package dev.Felix.rifa_system.Mapper;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResumoResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.ReservaResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CompraMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Converte Entity para Response básica
     */
    public CompraResponse toResponse(Compra compra, List<Integer> numeros) {
        return CompraResponse.builder()
                .id(compra.getId())
                .rifaId(compra.getRifaId())
                .tituloRifa(compra.getRifa() != null ? compra.getRifa().getTitulo() : null)
                .compradorId(compra.getCompradorId())
                .nomeComprador(compra.getComprador() != null ? compra.getComprador().getNome() : null)
                .status(compra.getStatus())
                .valorTotal(compra.getValorTotal())
                .quantidadeNumeros(compra.getQuantidadeNumeros())
                .numeros(numeros)
                .dataExpiracao(compra.getDataExpiracao())
                .dataCriacao(compra.getDataCriacao())
                .dataAtualizacao(compra.getDataAtualizacao())
                .build();
    }

    /**
     * Converte Entity para Response de Reserva
     */
    public ReservaResponse toReservaResponse(Compra compra, List<Integer> numeros, String tituloRifa) {
        LocalDateTime agora = LocalDateTime.now();
        long minutosRestantes = Duration.between(agora, compra.getDataExpiracao()).toMinutes();
        int minutosParaExpirar = (int) Math.max(0, minutosRestantes);

        return ReservaResponse.builder()
                .compraId(compra.getId())
                .rifaId(compra.getRifaId())
                .tituloRifa(tituloRifa)
                .quantidadeNumeros(compra.getQuantidadeNumeros())
                .numeros(numeros)
                .valorTotal(compra.getValorTotal())
                .dataExpiracao(compra.getDataExpiracao())
                .minutosParaExpirar(minutosParaExpirar)
                .pagamento(null) // Será preenchido após gerar PIX
                .build();
    }

    /**
     * Converte Entity para Response resumida
     */
    public CompraResumoResponse toResumoResponse(Compra compra) {
        return CompraResumoResponse.builder()
                .id(compra.getId())
                .tituloRifa(compra.getRifa() != null ? compra.getRifa().getTitulo() : null)
                .quantidadeNumeros(compra.getQuantidadeNumeros())
                .valorTotal(compra.getValorTotal())
                .status(compra.getStatus().name())
                .dataCriacao(compra.getDataCriacao().format(FORMATTER))
                .build();
    }
}
