package dev.Felix.rifa_system.Mapper.DtoNumeros;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request para reservar números
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservarNumerosRequest {

    @NotNull(message = "ID da rifa é obrigatório")
    private UUID rifaId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    @Max(value = 100, message = "Quantidade máxima é 100")
    private Integer quantidade;
    private List<@Min(1) Integer> numeros;
}