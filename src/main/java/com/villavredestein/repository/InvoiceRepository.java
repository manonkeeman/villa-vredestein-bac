package com.villavredestein.repository;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStudent(User student);
    List<Invoice> findByStudentEmail(String email);
    List<Invoice> findByStatusIgnoreCase(String status);
    List<Invoice> findByStatusIgnoreCaseAndDueDateBefore(String status, LocalDate date);
    List<Invoice> findByStatusIgnoreCaseAndDueDateBetween(String status, LocalDate start, LocalDate end);
    boolean existsByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);
}