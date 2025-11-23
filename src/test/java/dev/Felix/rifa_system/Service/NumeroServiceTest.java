package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import org.checkerframework.checker.index.qual.EnsuresLTLengthOf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class NumeroServiceTest {

    @InjectMocks
    private NumeroService numeroService;

    @Mock
    private NumeroRepository numeroRepository;

    @Test
    @DisplayName("Deve listar números por rifa com paginação")
    void listarPorRifa() {

        //Arrange
        UUID rifaId = UUID.randomUUID();

        List<Numero> numeros = List.of(
                Numero.builder().numero(3).status(StatusNumero.DISPONIVEL).build(),
                Numero.builder().numero(7).status(StatusNumero.DISPONIVEL).build(),
                Numero.builder().numero(10).status(StatusNumero.DISPONIVEL).build()
        );

        Mockito.when(numeroRepository.findByRifaIdAndStatus(rifaId , StatusNumero.DISPONIVEL))
                .thenReturn(numeros);

        List<Integer> resultado = numeroService.listarNumerosDisponiveis(rifaId);
        //Assert
        assertEquals(List.of(3,7,10), resultado);
    }

    @Test
    @DisplayName("deve listar numeros vendidos")
    void listarNumerosVendidos(){
        //Arrange
        UUID rifaId = UUID.randomUUID();

        List<Numero> numerosVendidos = List.of(
                Numero.builder().numero(1).status(StatusNumero.VENDIDO).build(),
                Numero.builder().numero(5).status(StatusNumero.VENDIDO).build(),
                Numero.builder().numero(8).status(StatusNumero.VENDIDO).build()
        );

        Mockito.when(numeroRepository.findByRifaIdAndStatus(rifaId , StatusNumero.VENDIDO))
                .thenReturn(numerosVendidos);

        assertEquals(List.of(1,5,8), numeroService.listarNumerosVendidos(rifaId));
    }

    @Test
    @DisplayName("deve buscar numeros da compra")
    void buscarNumerosDaCompra(){
         //Arrange
        UUID compraId = UUID.randomUUID();
        List<Numero> numerosCompra = List.of(
                Numero.builder().numero(2).status(StatusNumero.RESERVADO).build(),
                Numero.builder().numero(4).status(StatusNumero.RESERVADO).build()
        );
        //Act
        Mockito.when(numeroRepository.findByCompraId(compraId))
                .thenReturn(numerosCompra);
        assertEquals(List.of(2,4), numeroService.buscarNumerosDaCompra(compraId));
    }

    @Test
    @DisplayName("deve retornar estatisticas dos numeros")
    void obterEstatisticas(){
        //Arrange
        UUID rifaId = UUID.randomUUID();

        Mockito.when(numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL))
                .thenReturn(10L);
        Mockito.when(numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.RESERVADO))
                .thenReturn(5L);
        Mockito.when(numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.VENDIDO))
                .thenReturn(15L);
        //Act
        Map<String, Object> estatisticas = numeroService.obterEstatisticas(rifaId);

        assertEquals(30L, estatisticas.get("total"));
        assertEquals(10L, estatisticas.get("disponiveis"));
        assertEquals(5L, estatisticas.get("reservados"));
        assertEquals(15L, estatisticas.get("vendidos"));
        assertEquals(50.0, estatisticas.get("percentualVendido"));
    }
}