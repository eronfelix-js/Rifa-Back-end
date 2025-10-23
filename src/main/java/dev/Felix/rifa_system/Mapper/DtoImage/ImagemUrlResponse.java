package dev.Felix.rifa_system.Mapper.DtoImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagemUrlResponse {
    private String url;
    private String thumbnailUrl;
}