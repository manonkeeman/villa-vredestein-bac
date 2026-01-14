package com.villavredestein.repository;

import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByIdDesc();

    List<Document> findByRoleAccessOrderByIdDesc(String roleAccess);

    List<Document> findByRoleAccessIgnoreCaseOrderByIdDesc(String roleAccess);

    List<Document> findByRoleAccessInIgnoreCaseOrderByIdDesc(List<String> roleAccess);

    @Query("SELECT d FROM Document d WHERE UPPER(d.roleAccess) = UPPER(:role) OR UPPER(d.roleAccess) = UPPER('ALL') ORDER BY d.id DESC")
    List<Document> findAccessibleForRole(@Param("role") String role);

    List<Document> findByUploadedByOrderByIdDesc(User uploadedBy);

    List<Document> findByRoleAccessAndUploadedByOrderByIdDesc(String roleAccess, User uploadedBy);

    Optional<Document> findByStoragePath(String storagePath);
}