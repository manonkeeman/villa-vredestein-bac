package com.villavredestein.repository;

import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByIdDesc();

    @Query("""
            SELECT d
            FROM Document d
            WHERE UPPER(d.roleAccess) = UPPER(:role)
               OR UPPER(d.roleAccess) = 'ROLE_ALL'
               OR UPPER(d.roleAccess) = 'ALL'
            ORDER BY d.id DESC
            """)
    List<Document> findAccessibleForRole(@Param("role") String role);

    @Query("""
            SELECT d
            FROM Document d
            WHERE UPPER(d.roleAccess) IN :roles
            ORDER BY d.id DESC
            """)
    List<Document> findByRoleAccessInIgnoreCaseOrderByIdDesc(@Param("roles") List<String> roles);

    List<Document> findByUploadedByOrderByIdDesc(User uploadedBy);

    List<Document> findByRoleAccessAndUploadedByOrderByIdDesc(String roleAccess, User uploadedBy);

    Optional<Document> findByStoragePath(String storagePath);
}