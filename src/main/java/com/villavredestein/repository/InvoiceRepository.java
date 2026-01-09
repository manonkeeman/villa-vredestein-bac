package com.villavredestein.repository;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.Invoice.InvoiceStatus;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // -------------------------
    // Basale listing / sorting
    // -------------------------

    List<Invoice> findAllByOrderByIdDesc();

    List<Invoice> findByStudentOrderByIdDesc(User student);

    // Via relation: student.email (Invoice heeft geen studentEmail veld)
    List<Invoice> findByStudent_EmailIgnoreCaseOrderByIdDesc(String email);

    // -------------------------
    // Status filters (enum)
    // -------------------------

    List<Invoice> findByStatusOrderByIdDesc(InvoiceStatus status);

    List<Invoice> findByStatusAndDueDateBeforeOrderByDueDateAsc(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatusAndDueDateBetweenOrderByDueDateAsc(InvoiceStatus status, LocalDate start, LocalDate end);

    // -------------------------
    // Uniekheid: 1 factuur per student per maand/jaar
    // -------------------------

    boolean existsByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);
}