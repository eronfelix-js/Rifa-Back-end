package dev.Felix.rifa_system.Mapper;


import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Mapper.DtoRifa.CriarRifaRequest;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaDetalhadaResponse;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaResponse;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaResumoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
public class RifaMapper {

    /**
     * Converte Request para Entity
     */
    public Rifa toEntity(CriarRifaRequest request, UUID usuarioId) {
        return Rifa.builder()
                .usuarioId(usuarioId)
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .imagemUrl(request.getImagemUrl())
                .quantidadeNumeros(request.getQuantidadeNumeros())
                .precoPorNumero(request.getPrecoPorNumero())
                .dataLimite(request.getDataLimite())
                .sortearAoVenderTudo(request.getSortearAoVenderTudo())
                .build();
    }

    /**
     * Converte Entity para Response básica
     */
    public RifaResponse toResponse(Rifa rifa) {
        return RifaResponse.builder()
                .id(rifa.getId())
                .usuarioId(rifa.getUsuarioId())
                .nomeVendedor(rifa.getUsuario() != null ? rifa.getUsuario().getNome() : null)
                .titulo(rifa.getTitulo())
                .descricao(rifa.getDescricao())
                .imagemUrl(rifa.getImagemUrl())
                .quantidadeNumeros(rifa.getQuantidadeNumeros())
                .precoPorNumero(rifa.getPrecoPorNumero())
                .valorTotal(rifa.calcularValorTotal())
                .status(rifa.getStatus())
                .dataInicio(rifa.getDataInicio())
                .dataLimite(rifa.getDataLimite())
                .dataSorteio(rifa.getDataSorteio())
                .numeroVencedor(rifa.getNumeroVencedor())
                .compradorVencedorId(rifa.getCompradorVencedorId())
                .sorteioAutomatico(rifa.getSorteioAutomatico())
                .sortearAoVenderTudo(rifa.getSortearAoVenderTudo())
                .dataCriacao(rifa.getDataCriacao())
                .dataAtualizacao(rifa.getDataAtualizacao())
                .build();
    }

    /**
     * Converte Entity para Response detalhada (com estatísticas)
     */
    public RifaDetalhadaResponse toDetalhadaResponse(Rifa rifa, Long disponiveis, Long reservados, Long vendidos) {
        long total = rifa.getQuantidadeNumeros();
        BigDecimal percentual = BigDecimal.ZERO;

        if (total > 0) {
            percentual = BigDecimal.valueOf(vendidos)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }

        BigDecimal valorArrecadado = rifa.getPrecoPorNumero()
                .multiply(BigDecimal.valueOf(vendidos));

        return RifaDetalhadaResponse.builder()
                .id(rifa.getId())
                .usuarioId(rifa.getUsuarioId())
                .nomeVendedor(rifa.getUsuario() != null ? rifa.getUsuario().getNome() : null)
                .titulo(rifa.getTitulo())
                .descricao(rifa.getDescricao())
                .imagemUrl(rifa.getImagemUrl())
                .quantidadeNumeros(rifa.getQuantidadeNumeros())
                .precoPorNumero(rifa.getPrecoPorNumero())
                .valorTotal(rifa.calcularValorTotal())
                .status(rifa.getStatus())
                .dataInicio(rifa.getDataInicio())
                .dataLimite(rifa.getDataLimite())
                .dataSorteio(rifa.getDataSorteio())
                .numeroVencedor(rifa.getNumeroVencedor())
                .compradorVencedorId(rifa.getCompradorVencedorId())
                .sorteioAutomatico(rifa.getSorteioAutomatico())
                .sortearAoVenderTudo(rifa.getSortearAoVenderTudo())
                .dataCriacao(rifa.getDataCriacao())
                .dataAtualizacao(rifa.getDataAtualizacao())
                // Estatísticas
                .numerosDisponiveis(disponiveis)
                .numerosReservados(reservados)
                .numerosVendidos(vendidos)
                .percentualVendido(percentual)
                .valorArrecadado(valorArrecadado)
                .build();
    }

    /**
     * Converte Entity para Response resumida (listagens)
     */
    public RifaResumoResponse toResumoResponse(Rifa rifa, Long disponiveis) {
        long total = rifa.getQuantidadeNumeros();
        long vendidos = total - disponiveis;

        BigDecimal percentual = BigDecimal.ZERO;
        if (total > 0) {
            percentual = BigDecimal.valueOf(vendidos)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }

        return RifaResumoResponse.builder()
                .id(rifa.getId())
                .titulo(rifa.getTitulo())
                .imagemUrl(rifa.getImagemUrl())
                .precoPorNumero(rifa.getPrecoPorNumero())
                .quantidadeNumeros(rifa.getQuantidadeNumeros())
                .numerosDisponiveis(disponiveis)
                .percentualVendido(percentual)
                .status(rifa.getStatus().name())
                .build();
    }
}