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

    List<Invoice> findAllByOrderByIdDesc();

    List<Invoice> findByStudentOrderByIdDesc(User student);

    List<Invoice> findByStudent_EmailIgnoreCaseOrderByIdDesc(String email);

    List<Invoice> findByStatusOrderByIdDesc(InvoiceStatus status);

    List<Invoice> findByStatusAndDueDateBeforeOrderByDueDateAsc(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatusAndDueDateBetweenOrderByDueDateAsc(InvoiceStatus status, LocalDate start, LocalDate end);

    boolean existsByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);
}