package dev.Felix.rifa_system.Mapper.DtoRifa;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request para criar uma nova rifa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarRifaRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    private String titulo;

    @Size(max = 5000, message = "Descrição não pode exceder 5000 caracteres")
    private String descricao;

    @NotNull(message = "Quantidade de números é obrigatória")
    @Min(value = 50, message = "Quantidade mínima é 50")
    @Max(value = 100000, message = "Quantidade máxima é 100.000")
    private Integer quantidadeNumeros;

    @NotNull(message = "Preço por número é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço mínimo é R$ 0,01")
    @DecimalMax(value = "1000.00", message = "Preço máximo é R$ 1.000,00")
    private BigDecimal precoPorNumero;

    private LocalDateTime dataLimite;

    @Builder.Default
    private Boolean sortearAoVenderTudo = true;
}