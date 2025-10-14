package dev.Felix.rifa_system.Mapper.DtoRifa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response para números disponíveis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumerosDisponiveisResponse {

    private Integer total;
    private Integer disponiveis;
    private Integer vendidos;
    private Integer reservados;
    private List<Integer> numerosDisponiveis;
}