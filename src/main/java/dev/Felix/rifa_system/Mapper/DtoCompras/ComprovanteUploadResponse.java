package dev.Felix.rifa_system.Mapper.DtoCompras;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprovanteUploadResponse {
    private UUID compraId;
    private String comprovanteUrl;
    private LocalDateTime dataUpload;
    private String mensagem;
}
