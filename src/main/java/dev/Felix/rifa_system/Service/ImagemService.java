package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Exceptions.ImagemException;
import dev.Felix.rifa_system.Mapper.DtoImage.ImagemUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        log.info("Validando arquivo: {}", file.getOriginalFilename());
        // 1️⃣ Arquivo existe?
        if (file == null || file.isEmpty()) {
            throw ImagemException.arquivoInvalido("Nenhum arquivo enviado");
        }
        // 2️⃣ Tamanho (5MB)
        if (file.getSize() > maxSize) {
            throw ImagemException.tamanhoExcedido(file.getSize(), maxSize);
        }
        // 3️⃣ Tipo MIME básico
        String contentType = file.getContentType();
        log.info("Content-Type: {}", contentType);
        if (contentType == null || !contentType.startsWith("image/")) {
            throw ImagemException.tipoNaoSuportado(contentType);
        }
        // 4️⃣ Extensão
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw ImagemException.arquivoInvalido("Nome do arquivo inválido");
        }
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        log.info("Extensão: {}", extension);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw ImagemException.arquivoInvalido("Extensão não permitida: " + extension);
        }
        log.info("✅ Validação básica OK - Cloudinary fará validação completa");
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