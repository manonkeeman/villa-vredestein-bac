package com.villavredestein.dto;

public record UploadResponseDTO(
        Long id,
        String fileName,
        String downloadUrl
) {
}