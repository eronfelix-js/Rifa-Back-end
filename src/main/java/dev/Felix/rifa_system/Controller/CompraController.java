package dev.Felix.rifa_system.Controller;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Enum.TipoRifa;
import dev.Felix.rifa_system.Mapper.CompraMapper;
import dev.Felix.rifa_system.Mapper.DtoCompras.AprovarCompraRequest;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.ComprovanteUploadResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.ReservaResponse;
import dev.Felix.rifa_system.Mapper.DtoNumeros.ReservarNumerosRequest;
import dev.Felix.rifa_system.Mapper.DtoPagamento.PagamentoPixResponse;
import dev.Felix.rifa_system.Mapper.PagamentoMapper;
import dev.Felix.rifa_system.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
@Slf4j
public class CompraController {

    private final CompraService compraService;
    private final PagamentoService pagamentoService;
    private final RifaService rifaService;
    private final NumeroService numeroService;
    private final CompraMapper compraMapper;
    private final PagamentoMapper pagamentoMapper;
    private final UsuarioService usuarioService;

    @PostMapping("/reservar")
    public ResponseEntity<ReservaResponse> reservar(
            @Valid @RequestBody ReservarNumerosRequest request,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/reservar - Reservando {} números da rifa {}",
                request.getQuantidade(), request.getRifaId());
        UUID compradorId = UUID.fromString(authentication.getName());
        Compra compra = compraService.reservarNumeros(
                request.getRifaId(),
                compradorId,
                request.getQuantidade(),
                request.getNumeros()
        );
        List<Integer> numeros = numeroService.buscarNumerosDaCompra(compra.getId());
        Rifa rifa = rifaService.buscarPorId(request.getRifaId());
        Usuario vendedor = usuarioService.buscarPorId(rifa.getUsuarioId());
        ReservaResponse response = compraMapper.toReservaResponse(
                compra, numeros, rifa.getTitulo(), rifa, vendedor
        );
        log.info("Números reservados com sucesso - Compra: {}", compra.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ MANTIDO: Upload de comprovante
     */
    @PostMapping("/{compraId}/comprovante")
    public ResponseEntity<ComprovanteUploadResponse> uploadComprovante(
            @PathVariable UUID compraId,
            @RequestParam("comprovante") MultipartFile arquivo,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/{}/comprovante", compraId);
        UUID compradorId = UUID.fromString(authentication.getName());
        Compra compra = compraService.uploadComprovante(compraId, arquivo, compradorId);
        ComprovanteUploadResponse response = ComprovanteUploadResponse.builder()
                .compraId(compra.getId())
                .comprovanteUrl(compra.getComprovanteUrl())
                .dataUpload(compra.getDataUploadComprovante())
                .mensagem("Comprovante enviado! Aguarde a aprovação do vendedor.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ MANTIDO: Vendedor aprovar compra
     */
    @PostMapping("/{compraId}/aprovar")
    public ResponseEntity<Void> aprovar(
            @PathVariable UUID compraId,
            @Valid @RequestBody AprovarCompraRequest request,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/{}/aprovar", compraId);
        UUID vendedorId = UUID.fromString(authentication.getName());
        compraService.aprovarCompra(compraId, vendedorId, request.getObservacao());
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ MANTIDO: Rejeitar compra
     */
    @PostMapping("/{compraId}/rejeitar")
    public ResponseEntity<Void> rejeitar(
            @PathVariable UUID compraId,
            @Valid @RequestBody AprovarCompraRequest request,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/{}/rejeitar", compraId);

        UUID vendedorId = UUID.fromString(authentication.getName());

        compraService.rejeitarCompra(compraId, vendedorId, request.getObservacao());

        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ MANTIDO: Listar compras pendentes com comprovante
     */
    @GetMapping("/rifa/{rifaId}/pendentes")
    public ResponseEntity<Page<CompraResponse>> listarPendentes(
            @PathVariable UUID rifaId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/rifa/{}/pendentes", rifaId);

        UUID vendedorId = UUID.fromString(authentication.getName());

        // Validar que é dono da rifa
        Rifa rifa = rifaService.buscarPorId(rifaId);
        if (!rifa.getUsuarioId().equals(vendedorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<Compra> compras = compraService.listarComprasPendentesComComprovante(rifaId, pageable);

        Page<CompraResponse> response = compras.map(compra -> {
            List<Integer> numeros = numeroService.buscarNumerosDaCompra(compra.getId());
            return compraMapper.toResponse(compra, numeros);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ ATUALIZADO: Gerar pagamento PIX com Mercado Pago
     * Agora com fallback para pagamento manual se MP falhar
     */
    @PostMapping("/{compraId}/pagamento/pix")
    public ResponseEntity<?> gerarPagamentoPix(
            @PathVariable UUID compraId,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/{}/pagamento/pix - Gerando pagamento PIX", compraId);

        UUID compradorId = UUID.fromString(authentication.getName());

        // Buscar compra e validar dono
        Compra compra = compraService.buscarPorId(compraId);
        if (!compra.getCompradorId().equals(compradorId)) {
            log.warn("Usuário {} tentou gerar PIX para compra de outro usuário", compradorId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validar tipo de rifa
        Rifa rifa = rifaService.buscarPorId(compra.getRifaId());
        if (rifa.getTipo() != TipoRifa.PAGA_AUTOMATICA) {
            log.warn("Tentativa de gerar PIX automático para rifa tipo: {}", rifa.getTipo());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Esta rifa não aceita pagamento automático")
            );
        }

        try {
            // ✅ NOVO: Tentar criar pagamento PIX no Mercado Pago
            log.info("🔄 Tentando gerar PIX via Mercado Pago...");

            Pagamento pagamento = pagamentoService.criarPagamentoPix(compraId);
            PagamentoPixResponse response = pagamentoMapper.toPixResponse(pagamento);

            log.info("✅ PIX gerado com sucesso via Mercado Pago");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Erro ao gerar PIX no Mercado Pago: {}", e.getMessage(), e);

            // ✅ NOVO: Fallback para pagamento manual
            log.warn("⚠️ Fallback ativado - Redirecionando para pagamento manual");

            Usuario vendedor = usuarioService.buscarPorId(rifa.getUsuarioId());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    FallbackPagamentoResponse.builder()
                            .erro("PIX automático indisponível no momento")
                            .mensagem("Por favor, faça o pagamento manualmente usando os dados abaixo e envie o comprovante")
                            .chavePix(vendedor.getChavePix())
                            .nomeVendedor(vendedor.getNome())
                            .valorPagar(compra.getValorTotal())
                            .compraId(compraId)
                            .urlUploadComprovante("/api/v1/compras/" + compraId + "/comprovante")
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> buscarPorId(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/{}", id);

        UUID usuarioId = UUID.fromString(authentication.getName());

        Compra compra = compraService.buscarPorId(id);

        // Validar acesso (dono da compra ou dono da rifa)
        Rifa rifa = rifaService.buscarPorId(compra.getRifaId());
        if (!compra.getCompradorId().equals(usuarioId) && !rifa.getUsuarioId().equals(usuarioId)) {
            log.warn("Usuário {} sem permissão para ver compra {}", usuarioId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Integer> numeros = numeroService.buscarNumerosDaCompra(id);
        CompraResponse response = compraMapper.toResponse(compra, numeros);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/minhas")
    public ResponseEntity<Page<CompraResponse>> listarMinhas(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/minhas");

        UUID compradorId = UUID.fromString(authentication.getName());

        Page<Compra> compras = compraService.listarPorComprador(compradorId, pageable);

        Page<CompraResponse> response = compras.map(compra -> {
            List<Integer> numeros = numeroService.buscarNumerosDaCompra(compra.getId());
            return compraMapper.toResponse(compra, numeros);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ MANTIDO: Listar compras de uma rifa
     */
    @GetMapping("/rifa/{rifaId}")
    public ResponseEntity<Page<CompraResponse>> listarPorRifa(
            @PathVariable UUID rifaId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/rifa/{}", rifaId);

        UUID usuarioId = UUID.fromString(authentication.getName());

        // Validar que é dono da rifa
        Rifa rifa = rifaService.buscarPorId(rifaId);
        if (!rifa.getUsuarioId().equals(usuarioId)) {
            log.warn("Usuário {} tentou ver vendas da rifa de outro vendedor", usuarioId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<Compra> compras = compraService.listarPorRifa(rifaId, pageable);

        Page<CompraResponse> response = compras.map(compra -> {
            List<Integer> numeros = numeroService.buscarNumerosDaCompra(compra.getId());
            return compraMapper.toResponse(compra, numeros);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ MANTIDO: Buscar números de uma compra
     */
    @GetMapping("/{id}/numeros")
    public ResponseEntity<List<Integer>> buscarNumeros(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/{}/numeros", id);

        UUID usuarioId = UUID.fromString(authentication.getName());

        Compra compra = compraService.buscarPorId(id);

        // Validar acesso
        Rifa rifa = rifaService.buscarPorId(compra.getRifaId());
        if (!compra.getCompradorId().equals(usuarioId) && !rifa.getUsuarioId().equals(usuarioId)) {
            log.warn("Usuário {} sem permissão para ver números da compra {}", usuarioId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Integer> numeros = numeroService.buscarNumerosDaCompra(id);
        return ResponseEntity.ok(numeros);
    }

    /**
     * ✅ MANTIDO: Consultar status de um pagamento
     */
    @GetMapping("/{compraId}/pagamento")
    public ResponseEntity<PagamentoPixResponse> consultarPagamento(
            @PathVariable UUID compraId,
            Authentication authentication
    ) {
        log.info("GET /api/v1/compras/{}/pagamento", compraId);

        UUID usuarioId = UUID.fromString(authentication.getName());

        // Validar acesso
        Compra compra = compraService.buscarPorId(compraId);
        if (!compra.getCompradorId().equals(usuarioId)) {
            log.warn("Usuário {} tentou ver pagamento de outro usuário", usuarioId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pagamento pagamento = pagamentoService.buscarPorCompra(compraId);
        PagamentoPixResponse response = pagamentoMapper.toPixResponse(pagamento);

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ NOVO: DTOs para responses
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String erro;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class FallbackPagamentoResponse {
        private String erro;
        private String mensagem;
        private String chavePix;
        private String nomeVendedor;
        private java.math.BigDecimal valorPagar;
        private UUID compraId;
        private String urlUploadComprovante;
    }
}