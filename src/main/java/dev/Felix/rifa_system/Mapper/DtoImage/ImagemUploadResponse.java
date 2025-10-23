package dev.Felix.rifa_system.Mapper.DtoImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagemUploadResponse {
    private String url;
    private String thumbnailUrl;
    private String publicId;
    private String filename;
    private Long size;
    private Integer width;
    private Integer height;
    private String format;
    private LocalDateTime uploadedAt;
    private String mensagem;
}