package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Entity.Sorteio;
import dev.Felix.rifa_system.Enum.MetodoSorteio;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import dev.Felix.rifa_system.Repository.SorteioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SorteioService {

    private final SorteioRepository sorteioRepository;
    private final NumeroRepository numeroRepository;
    private final RifaService rifaService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Realizar sorteio automático
     */
    @Transactional
    public Sorteio sortearAutomatico(UUID rifaId) {
        log.info("Realizando sorteio automático para rifa: {}", rifaId);
        return realizarSorteio(rifaId, MetodoSorteio.AUTOMATICO, null);
    }

    /**
     * Realizar sorteio manual (vendedor)
     */
    @Transactional
    public Sorteio sortearManual(UUID rifaId, UUID vendedorId) {
        log.info("Realizando sorteio manual para rifa: {} por vendedor: {}", rifaId, vendedorId);

        // Validar que é o dono da rifa
        Rifa rifa = rifaService.buscarPorId(rifaId);
        if (!rifa.getUsuarioId().equals(vendedorId)) {
            throw new BusinessException("Apenas o dono da rifa pode realizar o sorteio");
        }

        return realizarSorteio(rifaId, MetodoSorteio.MANUAL, null);
    }

    /**
     * Lógica principal de sorteio
     */
    private Sorteio realizarSorteio(UUID rifaId, MetodoSorteio metodo, String observacoes) {
        // Buscar rifa
        Rifa rifa = rifaService.buscarPorId(rifaId);

        // Validações
        if (rifa.isSorteada()) {
            throw new BusinessException("Rifa já foi sorteada");
        }

        if (sorteioRepository.existsByRifaId(rifaId)) {
            throw new BusinessException("Já existe um sorteio para esta rifa");
        }

        // Buscar apenas números VENDIDOS
        List<Numero> numerosVendidos = numeroRepository.findByRifaIdAndStatus(rifaId, StatusNumero.VENDIDO);

        if (numerosVendidos.isEmpty()) {
            throw new BusinessException("Não há números vendidos para sortear");
        }

        log.info("Total de números vendidos: {}", numerosVendidos.size());

        // Sortear número aleatório entre os vendidos
        int indiceVencedor = secureRandom.nextInt(numerosVendidos.size());
        Numero numeroVencedor = numerosVendidos.get(indiceVencedor);

        log.info("Número sorteado: {} (índice: {})", numeroVencedor.getNumero(), indiceVencedor);

        // Buscar comprador vencedor
        UUID compradorVencedorId = numeroVencedor.getCompra().getCompradorId();

        // Gerar hash de verificação
        String hashVerificacao = gerarHashVerificacao(rifaId, numeroVencedor.getNumero());

        // Criar registro de sorteio
        Sorteio sorteio = Sorteio.builder()
                .rifaId(rifaId)
                .numeroSorteado(numeroVencedor.getNumero())
                .compradorVencedorId(compradorVencedorId)
                .metodo(metodo)
                .hashVerificacao(hashVerificacao)
                .dataSorteio(LocalDateTime.now())
                .observacoes(observacoes)
                .build();

        sorteio = sorteioRepository.save(sorteio);

        // Atualizar rifa
        rifaService.marcarComoSorteada(rifaId, numeroVencedor.getNumero(), compradorVencedorId);

        log.info("Sorteio realizado com sucesso - ID: {} - Número: {}",
                sorteio.getId(), numeroVencedor.getNumero());

        return sorteio;
    }

    /**
     * Gerar hash de verificação (prova de integridade)
     */
    private String gerarHashVerificacao(UUID rifaId, Integer numero) {
        String dados = rifaId.toString() + "|" +
                numero + "|" +
                LocalDateTime.now().toString() + "|" +
                secureRandom.nextLong();

        return DigestUtils.sha256Hex(dados);
    }

    /**
     * Buscar sorteio de uma rifa
     */
    @Transactional(readOnly = true)
    public Sorteio buscarPorRifa(UUID rifaId) {
        return sorteioRepository.findByRifaId(rifaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio não encontrado para esta rifa"));
    }

    /**
     * Verificar se rifa já foi sorteada
     */
    @Transactional(readOnly = true)
    public boolean jaSorteada(UUID rifaId) {
        return sorteioRepository.existsByRifaId(rifaId);
    }
}