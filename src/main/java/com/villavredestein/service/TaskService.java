package com.villavredestein.service;

import com.villavredestein.model.Document;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Service
public class TaskService {

    public Document create(String fileName, String contentType, String storagePath) {
        Document d = new Document();
        d.setFileName(fileName);
        d.setContentType(contentType);
        d.setStoragePath(storagePath);
        d.setSize(0L);
        d.setUploadedAt(Instant.now());
        return d;
    }

    public Path resolvePath(Document doc) {
        return Paths.get(doc.getStoragePath());
    }
}