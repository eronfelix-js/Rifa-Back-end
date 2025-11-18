package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Enum.StatusCompra;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Enum.TipoRifa;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Repository.CompraRepository;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ImagemService imagemService;
    private final UsuarioService usuarioService;

    @Value("${app.reserva.tempo-expiracao-minutos:15}")
    private Integer tempoExpiracaoMinutos;

    @Value("${app.reserva.max-numeros-por-compra:100}")
    private Integer maxNumerosPorCompra;

    /**
     * MÉTODO PRINCIPAL - Reservar números
     * Detecta automaticamente se é rifa gratuita ou paga
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Compra reservarNumeros(UUID rifaId, UUID compradorId, Integer quantidade, List<Integer> numerosEspecificos) {
        log.info("Reservando {} números para rifa {} - Comprador: {}", quantidade, rifaId, compradorId);
        Rifa rifa = rifaService.buscarPorId(rifaId);
        validarRifa(rifa);
        validarQuantidade(quantidade);
        List<Numero> numeros = buscarNumerosDisponiveis(rifaId, quantidade, numerosEspecificos);
        if (rifa.getTipo() == TipoRifa.GRATUITA) {
            log.info("🎉 Rifa GRATUITA - Confirmação automática");
            return processarCompraGratuita(rifa, compradorId, numeros);
        } else {
            log.info("💰 Rifa PAGA - Aguardando pagamento");
            return processarCompraPaga(rifa, compradorId, numeros);
        }
    }

    private Compra processarCompraGratuita(Rifa rifa, UUID compradorId, List<Numero> numeros) {
        log.info("Processando compra gratuita - {} números", numeros.size());

        // Criar compra já CONFIRMADA
        Compra compra = Compra.builder()
                .rifaId(rifa.getId())
                .compradorId(compradorId)
                .status(StatusCompra.CONFIRMADO) // ✅ Já confirmado
                .valorTotal(BigDecimal.ZERO)
                .quantidadeNumeros(numeros.size())
                .dataExpiracao(LocalDateTime.now()) // Rifa grátis não expira
                .build();

        compra = compraRepository.save(compra);

        // Marcar números como VENDIDOS direto (não apenas reservados)
        final UUID compraId = compra.getId();
        numeros.forEach(numero -> {
            numero.setCompraId(compraId);
            numero.setStatus(StatusNumero.VENDIDO); // ✅ Direto para vendido
            numero.setDataVenda(LocalDateTime.now());
        });
        numeroRepository.saveAll(numeros);

        log.info("✅ Compra gratuita confirmada: {} - {} números vendidos", compra.getId(), numeros.size());

        // Verificar se completou a rifa
        verificarRifaCompleta(rifa.getId());

        return compra;
    }

    private Compra processarCompraPaga(Rifa rifa, UUID compradorId, List<Numero> numeros) {
        log.info("Processando compra paga - {} números", numeros.size());

        BigDecimal valorTotal = rifa.getPrecoPorNumero().multiply(BigDecimal.valueOf(numeros.size()));

        // Criar compra PENDENTE
        Compra compra = Compra.builder()
                .rifaId(rifa.getId())
                .compradorId(compradorId)
                .status(StatusCompra.PENDENTE) // ⏳ Aguardando pagamento
                .valorTotal(valorTotal)
                .quantidadeNumeros(numeros.size())
                .dataExpiracao(LocalDateTime.now().plusMinutes(tempoExpiracaoMinutos))
                .build();

        compra = compraRepository.save(compra);

        // Marcar números como RESERVADOS (não vendidos ainda)
        final UUID compraId = compra.getId();
        numeros.forEach(numero -> numero.reservar(compraId));
        numeroRepository.saveAll(numeros);

        log.info("⏳ Compra paga criada: {} - {} números reservados - Expira em {}min",
                compra.getId(), numeros.size(), tempoExpiracaoMinutos);

        return compra;
    }

    private List<Numero> buscarNumerosDisponiveis(UUID rifaId, Integer quantidade, List<Integer> numerosEspecificos) {
        List<Numero> numeros;

        if (numerosEspecificos != null && !numerosEspecificos.isEmpty()) {
            // Buscar números específicos
            numeros = numeroRepository.findNumerosEspecificosComLock(rifaId, numerosEspecificos);

            if (numeros.size() != numerosEspecificos.size()) {
                throw new BusinessException("Alguns números selecionados não estão disponíveis");
            }
        } else {
            // Buscar números aleatórios
            Pageable limit = PageRequest.of(0, quantidade);
            numeros = numeroRepository.findDisponiveisComLock(rifaId, limit);

            if (numeros.size() < quantidade) {
                throw new BusinessException(
                        String.format("Apenas %d números disponíveis", numeros.size())
                );
            }
        }

        return numeros;
    }
    private void validarRifa(Rifa rifa) {
        if (!rifa.isAtiva()) {
            throw new BusinessException("Rifa não está ativa");
        }
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }

        if (quantidade > maxNumerosPorCompra) {
            throw new BusinessException(
                    String.format("Quantidade máxima é %d números", maxNumerosPorCompra)
            );
        }
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
        log.info("✅ Pagamento confirmado - {} números vendidos", numeros.size());
        verificarRifaCompleta(compra.getRifaId());
    }

    private void verificarRifaCompleta(UUID rifaId) {
        if (rifaService.vendeuTodosNumeros(rifaId)) {
            log.info("🎉 Rifa {} vendeu todos os números!", rifaId);
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
        log.info("🔓 {} números liberados", numeros.size());
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
        log.debug("🧹 Executando limpeza de reservas expiradas");

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
    @Transactional
    public Compra uploadComprovante(UUID compraId, MultipartFile arquivo, UUID compradorId) {
        log.info("📸 Upload de comprovante - Compra: {}", compraId);
        // Buscar e validar compra
        Compra compra = buscarPorId(compraId);
        // Validar dono
        if (!compra.getCompradorId().equals(compradorId)) {
            throw new BusinessException("Você não pode enviar comprovante para esta compra");
        }
        // Validar status
        if (!compra.isPendente()) {
            throw new BusinessException("Compra já foi processada");
        }
        // Validar se já tem comprovante
        if (compra.temComprovante()) {
            log.warn("Compra já possui comprovante. Será substituído.");
            // Deletar comprovante antigo do Cloudinary
            imagemService.deletarImagem(extrairPublicId(compra.getComprovanteUrl()));
        }
        // Upload no Cloudinary
        var uploadResponse = imagemService.uploadImagemRifa(
                arquivo,
                "comprovantes/" + compraId
        );
        // Atualizar compra
        compra.setComprovanteUrl(uploadResponse.getUrl());
        compra.setDataUploadComprovante(LocalDateTime.now());
        compraRepository.save(compra);

        log.info("✅ Comprovante enviado com sucesso - URL: {}", uploadResponse.getUrl());

        return compra;
    }

    /**
     * Vendedor APROVAR compra
     */
    @Transactional
    public void aprovarCompra(UUID compraId, UUID vendedorId, String observacao) {
        log.info("✅ Aprovando compra: {} - Vendedor: {}", compraId, vendedorId);

        Compra compra = buscarPorId(compraId);

        // Validar que é o dono da rifa
        Rifa rifa = rifaService.buscarPorId(compra.getRifaId());
        if (!rifa.getUsuarioId().equals(vendedorId)) {
            throw new BusinessException("Apenas o dono da rifa pode aprovar");
        }

        // Validar status
        if (!compra.isPendente()) {
            throw new BusinessException("Compra já foi processada");
        }

        // Validar comprovante
        if (!compra.temComprovante()) {
            throw new BusinessException("Compra não possui comprovante");
        }

        // Confirmar pagamento (reutiliza lógica existente)
        compra.confirmarPagamento();
        compra.setObservacaoVendedor(observacao);
        compra.setDataConfirmacao(LocalDateTime.now());
        compraRepository.save(compra);

        // Marcar números como vendidos
        List<Numero> numeros = numeroRepository.findByCompraId(compraId);
        numeros.forEach(Numero::vender);
        numeroRepository.saveAll(numeros);

        log.info("✅ Compra aprovada - {} números vendidos", numeros.size());

        verificarRifaCompleta(compra.getRifaId());
    }
    @Transactional
    public void rejeitarCompra(UUID compraId, UUID vendedorId, String observacao) {
        log.info("❌ Rejeitando compra: {} - Vendedor: {}", compraId, vendedorId);

        Compra compra = buscarPorId(compraId);

        // Validar que é o dono da rifa
        Rifa rifa = rifaService.buscarPorId(compra.getRifaId());
        if (!rifa.getUsuarioId().equals(vendedorId)) {
            throw new BusinessException("Apenas o dono da rifa pode rejeitar");
        }

        // Validar status
        if (!compra.isPendente()) {
            throw new BusinessException("Compra já foi processada");
        }
        // Rejeitar
        compra.cancelar();
        compra.setObservacaoVendedor(observacao);
        compraRepository.save(compra);
        // Liberar números
        liberarNumeros(compraId);
        log.info("❌ Compra rejeitada - Números liberados");
    }

    /**
     * Listar compras PENDENTES com comprovante (para vendedor aprovar)
     */
    @Transactional(readOnly = true)
    public Page<Compra> listarComprasPendentesComComprovante(UUID rifaId, Pageable pageable) {
        // Implementar no repository
        return compraRepository.findByRifaIdAndStatusAndComprovanteUrlIsNotNull(
                rifaId,
                StatusCompra.PENDENTE,
                pageable
        );
    }
    private String extrairPublicId(String url) {
        if (url == null || url.isEmpty()) return null;

        // URL formato: https://res.cloudinary.com/{cloud}/image/upload/{publicId}.jpg
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.substring(0, lastPart.lastIndexOf('.'));
    }
}
