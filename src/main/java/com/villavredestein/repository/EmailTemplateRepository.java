package com.villavredestein.repository;

import com.villavredestein.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByType(EmailTemplate.TemplateType type);

    boolean existsByType(EmailTemplate.TemplateType type);
}