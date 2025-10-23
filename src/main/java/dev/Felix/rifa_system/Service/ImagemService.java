package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Exceptions.ImagemException;
import dev.Felix.rifa_system.Mapper.DtoImage.ImagemUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImagemService {

    private final CloudnaryService cloudinaryService;

    private int maxSize = 5242880; // 5MB padrão

    private String allowedTypes = "image/jpeg,image/png,image/webp";

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    /**
     * Upload de imagem de rifa
     */
    public ImagemUploadResponse uploadImagemRifa(MultipartFile file, String rifaId) {
        log.info("Upload de imagem para rifa: {}", rifaId);
        validarArquivo(file);
        String folder = "rifas/" + rifaId;
        return cloudinaryService.upload(file, folder);
    }

    public ImagemUploadResponse uploadAvatar(MultipartFile file, String usuarioId) {
        log.info("Upload de avatar para usuário: {}", usuarioId);
        validarArquivo(file);
        String folder = "avatars/" + usuarioId;
        return cloudinaryService.upload(file, folder);
    }

    private void validarArquivo(MultipartFile file) {
        // Verificar se arquivo foi enviado
        if (file == null || file.isEmpty()) {
            throw ImagemException.arquivoInvalido("Nenhum arquivo enviado");
        }

        // Verificar tamanho
        if (file.getSize() > maxSize) {
            throw ImagemException.tamanhoExcedido(file.getSize(), maxSize);
        }

        // Verificar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw ImagemException.tipoNaoSuportado(contentType);
        }

        // Verificar extensão
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw ImagemException.arquivoInvalido("Extensão não permitida: " + extension);
        }

        // Verificar se é realmente uma imagem
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw ImagemException.arquivoInvalido("Arquivo não é uma imagem válida");
            }

            // Verificar dimensões mínimas
            if (image.getWidth() < 200 || image.getHeight() < 200) {
                throw ImagemException.arquivoInvalido(
                        String.format("Imagem muito pequena: %dx%d. Mínimo: 200x200",
                                image.getWidth(), image.getHeight())
                );
            }

            log.info("Imagem validada - Dimensões: {}x{}", image.getWidth(), image.getHeight());

        } catch (IOException e) {
            log.error("Erro ao validar imagem", e);
            throw ImagemException.arquivoInvalido("Não foi possível ler o arquivo");
        }
    }

    /**
     * Deletar imagem
     */
    public void deletarImagem(String publicId) {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinaryService.deletar(publicId);
        }
    }
}