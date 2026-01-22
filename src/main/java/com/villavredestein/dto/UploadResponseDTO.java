package com.villavredestein.dto;

public record UploadResponseDTO(
        Long documentId,
        String title,
        String downloadUrl
) {
}