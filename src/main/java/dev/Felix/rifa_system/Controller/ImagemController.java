package dev.Felix.rifa_system.Controller;

import dev.Felix.rifa_system.Mapper.DtoImage.ImagemUploadResponse;
import dev.Felix.rifa_system.Service.ImagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imagens")
@RequiredArgsConstructor
@Slf4j
public class ImagemController {

    private final ImagemService imagemService;

    /**
     * Upload de imagem para rifa
     */
    @PostMapping(value = "/rifa/{rifaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagemUploadResponse> uploadImagemRifa(
            @PathVariable String rifaId,
            @RequestParam("imagem") MultipartFile file,
            Authentication authentication
    ) {
        log.info("POST /api/v1/imagens/rifa/{} - Upload de imagem", rifaId);

        // Você pode adicionar validação se o usuário é dono da rifa

        ImagemUploadResponse response = imagemService.uploadImagemRifa(file, rifaId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Upload de avatar de usuário
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagemUploadResponse> uploadAvatar(
            @RequestParam("imagem") MultipartFile file,
            Authentication authentication
    ) {
        log.info("POST /api/v1/imagens/avatar - Upload de avatar");

        UUID usuarioId = UUID.fromString(authentication.getName());

        ImagemUploadResponse response = imagemService.uploadAvatar(file, usuarioId.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletar imagem
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deletarImagem(
            @PathVariable String publicId,
            Authentication authentication
    ) {
        log.info("DELETE /api/v1/imagens/{}", publicId);

        // Adicione validação de permissão aqui

        imagemService.deletarImagem(publicId);

        return ResponseEntity.noContent().build();
    }
}
