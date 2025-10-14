package dev.Felix.rifa_system.Service;


import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NumeroService {

    private final NumeroRepository numeroRepository;

    @Transactional(readOnly = true)
    public Page<Numero> listarPorRifa(UUID rifaId, Pageable pageable) {
        // Por performance, retornamos apenas números disponíveis e reservados
        // Números vendidos são muitos e podem ser consultados separadamente
        List<Numero> numeros = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), numeros.size());

        List<Numero> pageContent = numeros.subList(start, end);
        return new PageImpl<>(pageContent, pageable, numeros.size());
    }

    /**
     * Listar apenas números disponíveis
     */
    @Transactional(readOnly = true)
    public List<Integer> listarNumerosDisponiveis(UUID rifaId) {
        List<Numero> numeros = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL);
        return numeros.stream()
                .map(Numero::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Listar números vendidos
     */
    @Transactional(readOnly = true)
    public List<Integer> listarNumerosVendidos(UUID rifaId) {
        List<Numero> numeros = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.VENDIDO);
        return numeros.stream()
                .map(Numero::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Listar números reservados
     */
    @Transactional(readOnly = true)
    public List<Integer> listarNumerosReservados(UUID rifaId) {
        List<Numero> numeros = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.RESERVADO);
        return numeros.stream()
                .map(Numero::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Buscar números de uma compra
     */
    @Transactional(readOnly = true)
    public List<Integer> buscarNumerosDaCompra(UUID compraId) {
        List<Numero> numeros = numeroRepository.findByCompraId(compraId);
        return numeros.stream()
                .map(Numero::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Obter estatísticas de números da rifa
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticas(UUID rifaId) {
        long disponiveis = numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL);
        long reservados = numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.RESERVADO);
        long vendidos = numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.VENDIDO);
        long total = disponiveis + reservados + vendidos;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("disponiveis", disponiveis);
        stats.put("reservados", reservados);
        stats.put("vendidos", vendidos);
        stats.put("percentualVendido", total > 0 ? (vendidos * 100.0 / total) : 0);

        return stats;
    }

    /**
     * Verificar se números específicos estão disponíveis
     */
    @Transactional(readOnly = true)
    public boolean numerosEstaoDisponiveis(UUID rifaId, List<Integer> numeros) {
        if (numeros == null || numeros.isEmpty()) {
            return false;
        }

        List<Numero> numerosDisponiveis = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL);
        List<Integer> numerosDisponiveisList = numerosDisponiveis.stream()
                .map(Numero::getNumero)
                .collect(Collectors.toList());

        return numerosDisponiveisList.containsAll(numeros);
    }

    /**
     * Contar números por status
     */
    @Transactional(readOnly = true)
    public long contarPorStatus(UUID rifaId, StatusNumero status) {
        return numeroRepository.countByRifaIdAndStatus(rifaId, status);
    }
}