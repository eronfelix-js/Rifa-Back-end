package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Enum.StatusPagamento;
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

    @Transactional
    public Pagamento criarPagamentoPix(UUID compraId, String qrCode, String qrCodePayload) {
        log.info("Criando pagamento PIX para compra: {}", compraId);

        if (pagamentoRepository.existsByCompraId(compraId)) {
            throw new BusinessException("Pagamento já existe para esta compra");
        }

        Compra compra = compraService.buscarPorId(compraId);

        if (!compra.isPendente()) {
            throw new BusinessException("Compra não está pendente");
        }

        Pagamento pagamento = Pagamento.builder()
                .compraId(compraId)
                .gateway("PICPAY")
                .referenceId(compraId.toString())
                .qrCode(qrCode)
                .qrCodePayload(qrCodePayload)
                .valor(compra.getValorTotal())
                .status(StatusPagamento.AGUARDANDO)
                .dataExpiracao(compra.getDataExpiracao())
                .build();

        pagamento = pagamentoRepository.save(pagamento);
        log.info("Pagamento PIX criado: {}", pagamento.getId());

        return pagamento;
    }

    @Transactional
    public void aprovarPagamento(String referenceId, String authorizationId) {
        log.info("Aprovando pagamento - Reference: {}", referenceId);

        Pagamento pagamento = buscarPorReferenceId(referenceId);

        if (pagamento.isAprovado()) {
            log.info("Pagamento já aprovado anteriormente");
            return;
        }

        pagamento.aprovar(authorizationId);
        pagamentoRepository.save(pagamento);

        compraService.confirmarPagamento(pagamento.getCompraId());

        log.info("Pagamento aprovado com sucesso");
    }

    @Transactional
    public void recusarPagamento(String referenceId) {
        log.info("Recusando pagamento - Reference: {}", referenceId);

        Pagamento pagamento = buscarPorReferenceId(referenceId);
        pagamento.recusar();
        pagamentoRepository.save(pagamento);

        compraService.liberarNumeros(pagamento.getCompraId());

        log.info("Pagamento recusado");
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPorId(UUID id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPorReferenceId(String referenceId) {
        return pagamentoRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + referenceId));
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPorCompra(UUID compraId) {
        return pagamentoRepository.findByCompraId(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para compra: " + compraId));
    }
}