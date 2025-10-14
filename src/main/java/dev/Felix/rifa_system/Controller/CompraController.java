package dev.Felix.rifa_system.Controller;


import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Pagamento;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Mapper.CompraMapper;
import dev.Felix.rifa_system.Mapper.DtoCompras.CompraResponse;
import dev.Felix.rifa_system.Mapper.DtoCompras.ReservaResponse;
import dev.Felix.rifa_system.Mapper.DtoNumeros.ReservarNumerosRequest;
import dev.Felix.rifa_system.Mapper.DtoPagamento.PagamentoPixResponse;
import dev.Felix.rifa_system.Mapper.PagamentoMapper;
import dev.Felix.rifa_system.Service.CompraService;
import dev.Felix.rifa_system.Service.NumeroService;
import dev.Felix.rifa_system.Service.PagamentoService;
import dev.Felix.rifa_system.Service.RifaService;
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

    /**
     * Reservar números (CLIENTE)
     */
    @PostMapping("/reservar")
     public ResponseEntity<ReservaResponse> reservar(
            @Valid @RequestBody ReservarNumerosRequest request,
            Authentication authentication
    ) {
        log.info("POST /api/v1/compras/reservar - Reservando {} números da rifa {}",
                request.getQuantidade(), request.getRifaId());

        UUID compradorId = UUID.fromString(authentication.getName());

        // Reservar números
        Compra compra = compraService.reservarNumeros(
                request.getRifaId(),
                compradorId,
                request.getQuantidade(),
                request.getNumeros()
        );

        // Buscar números reservados
        List<Integer> numeros = numeroService.buscarNumerosDaCompra(compra.getId());

        // Buscar título da rifa
        Rifa rifa = rifaService.buscarPorId(request.getRifaId());

        // Criar response da reserva
        ReservaResponse response = compraMapper.toReservaResponse(compra, numeros, rifa.getTitulo());

        log.info("Números reservados com sucesso - Compra: {}", compra.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gerar pagamento PIX após reserva
     */
    @PostMapping("/{compraId}/pagamento/pix")
    public ResponseEntity<PagamentoPixResponse> gerarPagamentoPix(
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

        // TODO: Integrar com PicPay para gerar QR Code real
        // Por enquanto, vamos criar um pagamento mockado
        String qrCodeMock = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        String qrCodePayloadMock = "00020126580014br.gov.bcb.pix0136" + compraId.toString();

        Pagamento pagamento = pagamentoService.criarPagamentoPix(
                compraId,
                qrCodeMock,
                qrCodePayloadMock
        );

        PagamentoPixResponse response = pagamentoMapper.toPixResponse(pagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Buscar compra por ID
     */
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

    /**
     * Listar minhas compras (CLIENTE)
     */
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
     * Listar compras de uma rifa (VENDEDOR)
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
     * Buscar números de uma compra
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
     * Consultar status de um pagamento
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
}