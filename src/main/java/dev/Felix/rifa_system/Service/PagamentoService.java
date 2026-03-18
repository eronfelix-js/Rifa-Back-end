package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final CompraService compraService;
    private final UsuarioService usuarioService;

    @Transactional
    public Pagamento criarPagamentoPix(UUID compraId) {
        throw new BusinessException("Pagamento PIX temporariamente indisponível");
    }

    /**
     * ✅ NOVO: Aprovar pagamento usando external_reference (compraId)
     * Chamado pelo webhook do Mercado Pago
     */
    @Transactional
    public void aprovarPagamentoPorCompraId(UUID compraId, String authorizationCode) {
        log.info("✅ Aprovando pagamento via compraId: {}", compraId);

        Pagamento pagamento = pagamentoRepository.findByCompraId(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para compra: " + compraId));

        // Verificar se já foi aprovado (idempotência)
        if (pagamento.isAprovado()) {
            log.info("ℹ️ Pagamento já estava aprovado anteriormente");
            return;
        }

        // Aprovar pagamento
        pagamento.aprovar(authorizationCode);
        pagamentoRepository.save(pagamento);

        // Confirmar compra (marcar números como vendidos)
        compraService.confirmarPagamento(compraId);

        log.info("✅ Pagamento aprovado com sucesso - Compra: {}", compraId);
    }

    /**
     * ✅ MANTIDO: Aprovar pagamento (método legado - ainda usado pelo webhook)
     */
    @Transactional
    public void aprovarPagamento(String referenceId, String authorizationId) {
        log.info("✅ Aprovando pagamento - Reference: {}", referenceId);

        Pagamento pagamento = buscarPorReferenceId(referenceId);

        if (pagamento.isAprovado()) {
            log.info("ℹ️ Pagamento já aprovado anteriormente");
            return;
        }

        pagamento.aprovar(authorizationId);
        pagamentoRepository.save(pagamento);

        compraService.confirmarPagamento(pagamento.getCompraId());

        log.info("✅ Pagamento aprovado com sucesso");
    }

    /**
     * ✅ MANTIDO: Recusar pagamento
     */
    @Transactional
    public void recusarPagamento(String referenceId) {
        log.info("❌ Recusando pagamento - Reference: {}", referenceId);

        Pagamento pagamento = buscarPorReferenceId(referenceId);
        pagamento.recusar();
        pagamentoRepository.save(pagamento);

        compraService.liberarNumeros(pagamento.getCompraId());

        log.info("❌ Pagamento recusado");
    }

    /**
     * ✅ NOVO: Recusar pagamento por compraId
     */
    @Transactional
    public void recusarPagamentoPorCompraId(UUID compraId) {
        log.info("❌ Recusando pagamento - CompraId: {}", compraId);

        Pagamento pagamento = pagamentoRepository.findByCompraId(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para compra: " + compraId));

        pagamento.recusar();
        pagamentoRepository.save(pagamento);

        compraService.liberarNumeros(compraId);

        log.info("❌ Pagamento recusado - Números liberados");
    }

    /**
     * ✅ MANTIDO: Buscar por ID
     */
    @Transactional(readOnly = true)
    public Pagamento buscarPorId(UUID id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado"));
    }

    /**
     * ✅ MANTIDO: Buscar por reference ID
     */
    @Transactional(readOnly = true)
    public Pagamento buscarPorReferenceId(String referenceId) {
        return pagamentoRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + referenceId));
    }

    /**
     * ✅ MANTIDO: Buscar por compra
     */
    @Transactional(readOnly = true)
    public Pagamento buscarPorCompra(UUID compraId) {
        return pagamentoRepository.findByCompraId(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para compra: " + compraId));
    }
}
