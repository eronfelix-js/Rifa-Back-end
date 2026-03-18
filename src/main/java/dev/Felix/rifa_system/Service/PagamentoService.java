package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Enum.StatusPagamento;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;

import dev.Felix.rifa_system.Integração.Dto.PixResponse;
import dev.Felix.rifa_system.Integração.Exception.MercadoPagoException;
import dev.Felix.rifa_system.Integração.MercadoPago.MercadoPagoService;
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
    private final MercadoPagoService mercadoPagoService; // ✅ NOVO

    @Transactional
    public Pagamento criarPagamentoPix(UUID compraId) {
        log.info("💰 Criando pagamento PIX para compra: {}", compraId);

        // Validar se já existe pagamento
        if (pagamentoRepository.existsByCompraId(compraId)) {
            log.warn("⚠️ Pagamento já existe para compra {}", compraId);
            return pagamentoRepository.findByCompraId(compraId)
                    .orElseThrow(() -> new BusinessException("Erro ao buscar pagamento existente"));
        }

        // Buscar compra
        Compra compra = compraService.buscarPorId(compraId);

        // Validar status
        if (!compra.isPendente()) {
            throw new BusinessException("Compra não está pendente");
        }

        // Buscar dados do comprador
        Usuario comprador = usuarioService.buscarPorId(compra.getCompradorId());

        try {
            // ✅ NOVO: Chamar Mercado Pago para gerar PIX
            PixResponse pixResponse = mercadoPagoService.criarPagamentoPix(compra, comprador);

            // Criar registro de pagamento no banco
            Pagamento pagamento = Pagamento.builder()
                    .compraId(compraId)
                    .gateway("MERCADOPAGO") // ✅ MUDOU de PICPAY
                    .referenceId(pixResponse.getId().toString()) // ✅ MUDOU: agora é o payment ID do MP
                    .qrCode(pixResponse.getQrCodeBase64())
                    .qrCodePayload(pixResponse.getQrCode())
                    .valor(compra.getValorTotal())
                    .status(StatusPagamento.AGUARDANDO)
                    .dataExpiracao(compra.getDataExpiracao())
                    .build();
            pagamento = pagamentoRepository.save(pagamento);
            log.info("✅ Pagamento PIX criado - ID: {} - MP Payment ID: {}",
                    pagamento.getId(), pixResponse.getId());
            return pagamento;
        } catch (MercadoPagoException e) {
            log.error("❌ Erro ao criar PIX no Mercado Pago: {}", e.getMessage());
            throw new BusinessException("Erro ao gerar pagamento PIX: " + e.getMessage(), e);
        }
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

    /**
     * ✅ NOVO: Verificar se Mercado Pago está disponível
     */
    public boolean mercadoPagoDisponivel() {
        try {
            return mercadoPagoService.verificarConexao();
        } catch (Exception e) {
            log.error("❌ Mercado Pago indisponível: {}", e.getMessage());
            return false;
        }
    }
}
