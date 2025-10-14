package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Enum.StatusCompra;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Repository.CompraRepository;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import dev.Felix.rifa_system.Service.RifaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private final CompraRepository compraRepository;
    private final NumeroRepository numeroRepository;
    private final RifaService rifaService;

    @Value("${app.reserva.tempo-expiracao-minutos:15}")
    private Integer tempoExpiracaoMinutos;

    @Value("${app.reserva.max-numeros-por-compra:100}")
    private Integer maxNumerosPorCompra;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Compra reservarNumeros(UUID rifaId, UUID compradorId, Integer quantidade, List<Integer> numerosEspecificos) {
        log.info("Reservando {} números para rifa {} - Comprador: {}", quantidade, rifaId, compradorId);

        Rifa rifa = rifaService.buscarPorId(rifaId);
        if (!rifa.isAtiva()) {
            throw new BusinessException("Rifa não está ativa");
        }

        if (quantidade <= 0 || quantidade > maxNumerosPorCompra) {
            throw new BusinessException(
                    String.format("Quantidade deve estar entre 1 e %d", maxNumerosPorCompra)
            );
        }

        List<Numero> numeros;
        if (numerosEspecificos != null && !numerosEspecificos.isEmpty()) {
            numeros = numeroRepository.findNumerosEspecificosComLock(rifaId, numerosEspecificos);

            if (numeros.size() != numerosEspecificos.size()) {
                throw new BusinessException("Alguns números selecionados não estão disponíveis");
            }
        } else {
            Pageable limit = PageRequest.of(0, quantidade);
            numeros = numeroRepository.findDisponiveisComLock(rifaId, limit);

            if (numeros.size() < quantidade) {
                throw new BusinessException(
                        String.format("Apenas %d números disponíveis", numeros.size())
                );
            }
        }

        BigDecimal valorTotal = rifa.getPrecoPorNumero().multiply(BigDecimal.valueOf(numeros.size()));

        Compra compra = Compra.builder()
                .rifaId(rifaId)
                .compradorId(compradorId)
                .status(StatusCompra.PENDENTE)
                .valorTotal(valorTotal)
                .quantidadeNumeros(numeros.size())
                .dataExpiracao(LocalDateTime.now().plusMinutes(tempoExpiracaoMinutos))
                .build();

        compra = compraRepository.save(compra);

        final UUID compraId = compra.getId();
        numeros.forEach(numero -> numero.reservar(compraId));
        numeroRepository.saveAll(numeros);

        log.info("Compra criada com sucesso: {} - {} números reservados", compra.getId(), numeros.size());
        return compra;
    }

    @Transactional
    public void confirmarPagamento(UUID compraId) {
        log.info("Confirmando pagamento da compra: {}", compraId);

        Compra compra = buscarPorId(compraId);

        if (!compra.isPendente()) {
            log.warn("Compra {} já foi processada. Status: {}", compraId, compra.getStatus());
            return;
        }

        compra.confirmarPagamento();
        compraRepository.save(compra);

        List<Numero> numeros = numeroRepository.findByCompraId(compraId);
        numeros.forEach(Numero::vender);
        numeroRepository.saveAll(numeros);

        log.info("Pagamento confirmado - {} números vendidos", numeros.size());

        verificarRifaCompleta(compra.getRifaId());
    }

    private void verificarRifaCompleta(UUID rifaId) {
        if (rifaService.vendeuTodosNumeros(rifaId)) {
            log.info("Rifa {} vendeu todos os números!", rifaId);
            rifaService.marcarComoCompleta(rifaId);
        }
    }

    @Transactional
    public void expirarCompra(UUID compraId) {
        log.info("Expirando compra: {}", compraId);
        Compra compra = buscarPorId(compraId);
        compra.expirar();
        compraRepository.save(compra);
        liberarNumeros(compraId);
    }

    @Transactional
    public void liberarNumeros(UUID compraId) {
        log.info("Liberando números da compra: {}", compraId);
        List<Numero> numeros = numeroRepository.findByCompraId(compraId);
        numeros.forEach(Numero::liberar);
        numeroRepository.saveAll(numeros);
        log.info("{} números liberados", numeros.size());
    }

    @Transactional(readOnly = true)
    public Compra buscarPorId(UUID id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.compra(id.toString()));
    }

    @Transactional(readOnly = true)
    public Page<Compra> listarPorComprador(UUID compradorId, Pageable pageable) {
        return compraRepository.findByCompradorIdOrderByDataCriacaoDesc(compradorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Compra> listarPorRifa(UUID rifaId, Pageable pageable) {
        return compraRepository.findByRifaIdOrderByDataCriacaoDesc(rifaId, pageable);
    }

    @Transactional
    public int limparReservasExpiradas() {
        log.debug("Executando limpeza de reservas expiradas");

        List<Compra> comprasExpiradas = compraRepository.findByStatusAndDataExpiracaoBefore(
                StatusCompra.PENDENTE,
                LocalDateTime.now()
        );

        if (comprasExpiradas.isEmpty()) {
            return 0;
        }

        log.info("Encontradas {} compras expiradas", comprasExpiradas.size());

        comprasExpiradas.forEach(compra -> {
            expirarCompra(compra.getId());
        });

        return comprasExpiradas.size();
    }
}