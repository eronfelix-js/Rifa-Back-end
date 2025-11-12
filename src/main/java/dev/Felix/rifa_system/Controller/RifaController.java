package dev.Felix.rifa_system.Controller;


import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Mapper.DtoRifa.CriarRifaRequest;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaDetalhadaResponse;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaResponse;
import dev.Felix.rifa_system.Mapper.DtoRifa.RifaResumoResponse;
import dev.Felix.rifa_system.Mapper.RifaMapper;
import dev.Felix.rifa_system.Service.ImagemService;
import dev.Felix.rifa_system.Service.NumeroService;
import dev.Felix.rifa_system.Service.RifaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rifas")
@RequiredArgsConstructor
@Slf4j
public class RifaController {

    private final RifaService rifaService;
    private final NumeroService numeroService;
    private final RifaMapper rifaMapper;
    private final ImagemService imagemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RifaResponse> criar(
            @RequestPart("rifa") @Valid CriarRifaRequest request,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            Authentication authentication) {

        log.info("===== CONTROLLER - CRIAR RIFA =====");
        log.info("Request recebido: {}", request);
        log.info("Imagem recebida no controller: {}",
                imagem != null ? imagem.getOriginalFilename() : "NULL");
        log.info("Tamanho da imagem: {}",
                imagem != null ? imagem.getSize() : "N/A");

        UUID usuarioId = UUID.fromString(authentication.getName());

        Rifa rifaCriada = rifaService.criarComImagem(request, imagem, usuarioId);

        RifaResponse response = rifaMapper.toResponse(rifaCriada);

        log.info("Response: imagemUrl = {}", response.getImagemUrl());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listar rifas ativas (PÚBLICO)
     */
    @GetMapping
    public ResponseEntity<Page<RifaResumoResponse>> listarAtivas(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/v1/rifas - Listando rifas ativas");

        Page<Rifa> rifas = rifaService.listarAtivas(pageable);

        Page<RifaResumoResponse> response = rifas.map(rifa -> {
            Long disponiveis = numeroService.contarPorStatus(rifa.getId(), StatusNumero.DISPONIVEL);
            return rifaMapper.toResumoResponse(rifa, disponiveis);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Buscar rifa por ID (PÚBLICO)
     */
    @GetMapping("/{id}")
    public ResponseEntity<RifaDetalhadaResponse> buscarPorId(@PathVariable UUID id) {
        log.info("GET /api/v1/rifas/{} - Buscando rifa", id);

        Rifa rifa = rifaService.buscarPorId(id);

        Long disponiveis = numeroService.contarPorStatus(id, StatusNumero.DISPONIVEL);
        Long reservados = numeroService.contarPorStatus(id, StatusNumero.RESERVADO);
        Long vendidos = numeroService.contarPorStatus(id, StatusNumero.VENDIDO);

        RifaDetalhadaResponse response = rifaMapper.toDetalhadaResponse(
                rifa, disponiveis, reservados, vendidos
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Listar minhas rifas (VENDEDOR)
     */
    @GetMapping("/minhas")

    public ResponseEntity<List<RifaResponse>> listarMinhas(Authentication authentication) {
        log.info("GET /api/v1/rifas/minhas - Listando rifas do usuário");

        UUID usuarioId = UUID.fromString(authentication.getName());

        List<Rifa> rifas = rifaService.listarPorUsuario(usuarioId);

        List<RifaResponse> response = rifas.stream()
                .map(rifaMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar rifa (VENDEDOR)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        log.info("DELETE /api/v1/rifas/{} - Cancelando rifa", id);

        UUID usuarioId = UUID.fromString(authentication.getName());
        rifaService.cancelar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Listar números disponíveis da rifa (PÚBLICO)
     */
    @GetMapping("/{id}/numeros/disponiveis")
    public ResponseEntity<List<Integer>> listarNumerosDisponiveis(@PathVariable UUID id) {
        log.info("GET /api/v1/rifas/{}/numeros/disponiveis", id);

        List<Integer> numeros = numeroService.listarNumerosDisponiveis(id);

        return ResponseEntity.ok(numeros);
    }

    /**
     * Obter estatísticas da rifa (PÚBLICO)
     */
    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<?> obterEstatisticas(@PathVariable UUID id) {
        log.info("GET /api/v1/rifas/{}/estatisticas", id);

        return ResponseEntity.ok(numeroService.obterEstatisticas(id));
    }
}