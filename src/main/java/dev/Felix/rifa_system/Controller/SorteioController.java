package dev.Felix.rifa_system.Controller;

import dev.Felix.rifa_system.Entity.Sorteio;
import dev.Felix.rifa_system.Mapper.DtoSorteio.SorteioResponse;
import dev.Felix.rifa_system.Mapper.SorteioMapper;
import dev.Felix.rifa_system.Service.SorteioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sorteios")
@RequiredArgsConstructor
@Slf4j

public class SorteioController {

    private final SorteioService sorteioService;
    private final SorteioMapper sorteioMapper;

    @PostMapping("/rifa/{rifaId}/sortear")
    public ResponseEntity<SorteioResponse> sortearManual(
            @PathVariable UUID rifaId,
            Authentication authentication
    ) {
        log.info("POST /api/v1/sorteios/rifa/{}/sortear", rifaId);

        UUID vendedorId = UUID.fromString(authentication.getName());
        Sorteio sorteio = sorteioService.sortearManual(rifaId, vendedorId);
        SorteioResponse response = sorteioMapper.toResponse(sorteio);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/rifa/{rifaId}")
    public ResponseEntity<SorteioResponse> buscarPorRifa(@PathVariable UUID rifaId) {
        log.info("GET /api/v1/sorteios/rifa/{}", rifaId);

        Sorteio sorteio = sorteioService.buscarPorRifa(rifaId);
        SorteioResponse response = sorteioMapper.toResponse(sorteio);

        return ResponseEntity.ok(response);
    }
}