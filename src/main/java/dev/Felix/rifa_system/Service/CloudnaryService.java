package dev.Felix.rifa_system.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dev.Felix.rifa_system.Exceptions.ImagemException;
import dev.Felix.rifa_system.Mapper.DtoImage.ImagemUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudnaryService {

    private final Cloudinary cloudinary;

    public ImagemUploadResponse upload(MultipartFile file, String folder){
        log.info("iniciando uploud para a nuvem Cloudnary", file.getOriginalFilename());
        try {
            Map uploudResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", new com.cloudinary.Transformation()
                                    .width(1200).height(1200).crop("limit")
                                    .quality("auto:good")));

            String publicId = (String) uploudResult.get("public_id");
            String url = (String) uploudResult.get("secure_url");

            String ThumbnailUr= cloudinary.url()
                    .transformation(new com.cloudinary.Transformation()
                    .width(300).height(300).crop("fill")
                    .quality("auto:good"))
                    .generate(publicId);

            log.info("Upload concluído com sucesso - URL: {}", url);

            return ImagemUploadResponse.builder()
                    .url(url)
                    .thumbnailUrl(ThumbnailUr)
                    .publicId(publicId)
                    .filename(file.getOriginalFilename())
                    .size(file.getSize())
                    .width((Integer) uploudResult.get("width"))
                    .height((Integer) uploudResult.get("height"))
                    .format((String) uploudResult.get("format"))
                    .uploadedAt(java.time.LocalDateTime.now())
                    .mensagem("Upload realizado com sucesso")
                    .build();


        }catch (Exception e){
            log.error("Erro ao fazer upload da imagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer upload da imagem", e);
        }


    }

    public void deletar(String publicId){
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Imagem deletada - Resultado: {}", result.get("result"));

        } catch (IOException e) {
            log.error("Erro ao deletar imagem do Cloudinary", e);
            throw new ImagemException("Erro ao deletar imagem", e);
        }
    }
    public String getOptimizedUrl(String publicId, int width, int height) {
        return cloudinary.url()
                .transformation(new com.cloudinary.Transformation()
                        .width(width).height(height).crop("fill")
                        .quality("auto:good")
                        .fetchFormat("auto"))
                .generate(publicId);
    }

}
