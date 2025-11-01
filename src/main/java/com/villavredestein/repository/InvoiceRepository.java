package com.villavredestein.repository;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStudent(User student);

    List<Invoice> findByStudentEmail(String email);

    List<Invoice> findByStatusAndDueDateBefore(String status, LocalDate date);

    List<Invoice> findByStatusAndDueDateBetween(String status, LocalDate start, LocalDate end);

    boolean existsByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);
}