package dev.Felix.rifa_system.Mapper;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Enum.TipoRifa;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResumoResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.ReservaResponse;
import dev.Felix.rifa_system.Utils.TaxaCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
                .comprovanteUrl(compra.getComprovanteUrl())
                .dataUploadComprovante(compra.getDataUploadComprovante())
                .observacaoVendedor(compra.getObservacaoVendedor())
                .dataConfirmacao(compra.getDataConfirmacao())
                .build();
    }

    /**
     * ✅ ATUALIZADO: Converte Entity para Response de Reserva
     */
    public ReservaResponse toReservaResponse(
            Compra compra,
            List<Integer> numeros,
            String tituloRifa,
            Rifa rifa,
            Usuario vendedor
    ) {
        LocalDateTime agora = LocalDateTime.now();
        Integer minutosParaExpirar = null;

        if (compra.getDataExpiracao() != null) {
            long minutosRestantes = Duration.between(agora, compra.getDataExpiracao()).toMinutes();
            minutosParaExpirar = (int) Math.max(0, minutosRestantes);
        }

        TaxaCalculator.CalculoTaxa calculoTaxa;

        if(rifa.getTipo() == TipoRifa.PAGA_AUTOMATICA) {
            calculoTaxa = TaxaCalculator.calcular(
                    compra.getValorTotal(),
                    rifa.getRepassarTaxaCliente()
            );
        } else {
            calculoTaxa = TaxaCalculator.CalculoTaxa.builder()
                    .subtotal(compra.getValorTotal())
                    .valorTaxa(BigDecimal.ZERO)
                    .totalCliente(compra.getValorTotal())
                    .vendedorRecebe(compra.getValorTotal())
                    .taxaRepassada(false)
                    .taxaPercentual(BigDecimal.ZERO)
                    .build();
        }

        ReservaResponse.DadosPagamentoManual pagamentoManual = null;
        if (rifa.getTipo() == TipoRifa.PAGA_MANUAL) {
            pagamentoManual = ReservaResponse.DadosPagamentoManual.builder()
                    .chavePix(vendedor.getChavePix())
                    .nomeVendedor(vendedor.getNome())
                    .emailVendedor(vendedor.getEmail())
                    .valorPagar(compra.getValorTotal())  // ✅ Sem taxa aqui
                    .subtotal(compra.getValorTotal())
                    .valorTaxa(BigDecimal.ZERO)
                    .taxaRepassada(false)
                    .mensagem("Faça o pagamento via PIX e envie o comprovante")
                    .build();
        }

        return ReservaResponse.builder()
                .compraId(compra.getId())
                .rifaId(compra.getRifaId())
                .tituloRifa(tituloRifa)
                .tipoRifa(rifa.getTipo().name())
                .quantidadeNumeros(compra.getQuantidadeNumeros())
                .numeros(numeros)
                .subtotal(calculoTaxa.getSubtotal())
                .valorTaxa(calculoTaxa.getValorTaxa())
                .valorTotal(calculoTaxa.getTotalCliente())
                .taxaRepassada(calculoTaxa.getTaxaRepassada())
                .status(compra.getStatus())
                .dataExpiracao(compra.getDataExpiracao())
                .minutosParaExpirar(minutosParaExpirar)
                .pagamentoManual(pagamentoManual)
                .build();
    }

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