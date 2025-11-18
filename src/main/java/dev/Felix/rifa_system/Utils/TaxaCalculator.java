package dev.Felix.rifa_system.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxaCalculator {
    private static final BigDecimal TAXA_PIX_DEFAULT = new BigDecimal("0.0099");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static CalculoTaxa calcular(BigDecimal valorBase, boolean repassarCliente) {
        return calcular(valorBase, repassarCliente, TAXA_PIX_DEFAULT);
    }

    public static CalculoTaxa calcular(
            BigDecimal valorBase,
            boolean repassarCliente,
            BigDecimal taxaPercentual
    ) {
        // Validações
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor base deve ser maior que zero");
        }

        if (taxaPercentual == null || taxaPercentual.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa percentual deve ser maior ou igual a zero");
        }

        // Calcular valor da taxa em reais
        BigDecimal valorTaxa = valorBase
                .multiply(taxaPercentual)
                .setScale(SCALE, ROUNDING_MODE);

        BigDecimal subtotal;
        BigDecimal totalCliente;
        BigDecimal vendedorRecebe;

        if (repassarCliente) {
            // Cliente paga a taxa
            subtotal = valorBase;
            totalCliente = valorBase.add(valorTaxa);
            vendedorRecebe = valorBase; // Vendedor recebe valor cheio
        } else {
            // Vendedor absorve a taxa
            subtotal = valorBase;
            totalCliente = valorBase; // Cliente paga valor redondo
            vendedorRecebe = valorBase.subtract(valorTaxa); // Vendedor perde a taxa
        }

        return CalculoTaxa.builder()
                .subtotal(subtotal.setScale(SCALE, ROUNDING_MODE))
                .valorTaxa(valorTaxa)
                .totalCliente(totalCliente.setScale(SCALE, ROUNDING_MODE))
                .vendedorRecebe(vendedorRecebe.setScale(SCALE, ROUNDING_MODE))
                .taxaRepassada(repassarCliente)
                .taxaPercentual(taxaPercentual)
                .build();
    }

    /**
     * Calcula apenas o valor da taxa (sem objeto completo)
     */
    public static BigDecimal calcularValorTaxa(BigDecimal valorBase, BigDecimal taxaPercentual) {
        return valorBase
                .multiply(taxaPercentual)
                .setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Resultado do cálculo de taxa
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculoTaxa {

        /**
         * Valor base (sem taxa)
         */
        private BigDecimal subtotal;

        /**
         * Valor da taxa em reais
         */
        private BigDecimal valorTaxa;

        /**
         * Total que o cliente vai pagar
         */
        private BigDecimal totalCliente;

        /**
         * Valor que o vendedor vai receber (após taxa)
         */
        private BigDecimal vendedorRecebe;

        /**
         * Se a taxa foi repassada ao cliente
         */
        private Boolean taxaRepassada;

        /**
         * Taxa percentual usada no cálculo
         */
        private BigDecimal taxaPercentual;

        /**
         * Retorna descrição legível
         */
        public String getDescricao() {
            if (taxaRepassada) {
                return String.format(
                        "Subtotal: R$ %s + Taxa (%.2f%%): R$ %s = Total: R$ %s",
                        subtotal, taxaPercentual.multiply(new BigDecimal("100")), valorTaxa, totalCliente
                );
            } else {
                return String.format(
                        "Total: R$ %s (Taxa %.2f%% descontada do vendedor)",
                        totalCliente, taxaPercentual.multiply(new BigDecimal("100"))
                );
            }
        }
    }
}