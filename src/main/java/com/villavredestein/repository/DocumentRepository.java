package com.villavredestein.repository;

import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByIdDesc();

    List<Document> findByRoleAccessOrderByIdDesc(String roleAccess);

    List<Document> findByUploadedByOrderByIdDesc(User uploadedBy);

    List<Document> findByRoleAccessAndUploadedByOrderByIdDesc(String roleAccess, User uploadedBy);
    Optional<Document> findByStoragePath(String storagePath);
}