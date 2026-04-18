package com.villavredestein.controller;

import com.villavredestein.dto.EmailTemplateDTO;
import com.villavredestein.model.EmailTemplate.TemplateType;
import com.villavredestein.service.EmailTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only REST endpoints for managing email templates.
 *
 * GET  /api/admin/email-templates          → list all three templates
 * PUT  /api/admin/email-templates/{type}   → update subject + body
 */
@RestController
@RequestMapping(value = "/api/admin/email-templates", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplateController(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplateDTO>> getAll() {
        List<EmailTemplateDTO> dtos = emailTemplateService.getAll()
                .stream()
                .map(EmailTemplateDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping(value = "/{type}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailTemplateDTO> update(
            @PathVariable String type,
            @Valid @RequestBody EmailTemplateDTO body
    ) {
        TemplateType templateType;
        try {
            templateType = TemplateType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        var updated = emailTemplateService.update(templateType, body.getSubject(), body.getBody());
        return ResponseEntity.ok(new EmailTemplateDTO(updated));
    }
}