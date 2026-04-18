package com.villavredestein.dto;

import com.villavredestein.model.EmailTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class EmailTemplateDTO {

    private Long id;
    private String type;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    private String body;

    private LocalDateTime updatedAt;

    public EmailTemplateDTO() {}

    public EmailTemplateDTO(EmailTemplate t) {
        this.id = t.getId();
        this.type = t.getType().name();
        this.subject = t.getSubject();
        this.body = t.getBody();
        this.updatedAt = t.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }
}