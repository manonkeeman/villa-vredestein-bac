package com.villavredestein.dto;

import java.time.LocalDateTime;

public class AnnouncementResponseDTO {

    private final Long id;
    private final String type;
    private final String title;
    private final String body;
    private final String author;
    private final LocalDateTime createdAt;

    public AnnouncementResponseDTO(Long id, String type, String title, String body, String author, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}