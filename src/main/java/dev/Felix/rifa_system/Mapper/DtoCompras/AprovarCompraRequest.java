package dev.Felix.rifa_system.Mapper.DtoCompras;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprovarCompraRequest {
    @NotBlank(message = "Observação é obrigatória")
    private String observacao;
}