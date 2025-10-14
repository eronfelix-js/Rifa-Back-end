package dev.Felix.rifa_system.Mapper;

import dev.Felix.rifa_system.Entity.Sorteio;
import dev.Felix.rifa_system.Mapper.DtoSorteio.SorteioResponse;
import org.springframework.stereotype.Component;

@Component
public class SorteioMapper {

    public SorteioResponse toResponse(Sorteio sorteio) {
        return SorteioResponse.builder()
                .id(sorteio.getId())
                .rifaId(sorteio.getRifaId())
                .tituloRifa(sorteio.getRifa() != null ? sorteio.getRifa().getTitulo() : null)
                .numeroSorteado(sorteio.getNumeroSorteado())
                .compradorVencedorId(sorteio.getCompradorVencedorId())
                .nomeVencedor(sorteio.getCompradorVencedor() != null ? sorteio.getCompradorVencedor().getNome() : null)
                .emailVencedor(sorteio.getCompradorVencedor() != null ? sorteio.getCompradorVencedor().getEmail() : null)
                .metodo(sorteio.getMetodo())
                .hashVerificacao(sorteio.getHashVerificacao())
                .dataSorteio(sorteio.getDataSorteio())
                .observacoes(sorteio.getObservacoes())
                .build();
    }
}