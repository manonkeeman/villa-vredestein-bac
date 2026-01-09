package com.villavredestein.dto;

public class DocumentResponseDTO {

    private final Long id;
    private final String title;
    private final String description;
    private final String roleAccess;
    private final String uploadedBy;

    public DocumentResponseDTO(Long id,
                               String title,
                               String description,
                               String roleAccess,
                               String uploadedBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.roleAccess = roleAccess;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRoleAccess() {
        return roleAccess;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }
}