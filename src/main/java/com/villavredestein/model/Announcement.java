package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    public enum AnnouncementType {
        mededeling, onderhoud, evenement
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementType type = AnnouncementType.mededeling;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String title;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String body;

    @Size(max = 80)
    @Column(length = 80)
    private String author;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Announcement() {}

    public Long getId() { return id; }
    public AnnouncementType getType() { return type; }
    public void setType(AnnouncementType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
