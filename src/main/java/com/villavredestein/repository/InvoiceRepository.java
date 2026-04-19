package com.villavredestein.repository;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByIdDesc();

    List<Invoice> findByStudentOrderByIdDesc(User student);

    List<Invoice> findByStudent_EmailIgnoreCaseOrderByIdDesc(String email);

    List<Invoice> findByStatusOrderByIdDesc(Invoice.InvoiceStatus status);

    List<Invoice> findByStatusAndDueDateBeforeOrderByDueDateAsc(Invoice.InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatusAndDueDateBetweenOrderByDueDateAsc(Invoice.InvoiceStatus status, LocalDate start, LocalDate end);

    boolean existsByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);

    // Mollie webhook lookup
    Optional<Invoice> findByMolliePaymentId(String molliePaymentId);

    // For reminder jobs: all invoices of a given month/year that aren't paid/cancelled
    List<Invoice> findByInvoiceMonthAndInvoiceYearAndStatusNotIn(
            int invoiceMonth, int invoiceYear, List<Invoice.InvoiceStatus> excludedStatuses);

    // For admin overview: all invoices of a given month/year
    List<Invoice> findByInvoiceMonthAndInvoiceYearOrderByStudentUsernameAsc(int invoiceMonth, int invoiceYear);

    // For admin manual send: find invoices for a specific student + month
    List<Invoice> findByStudentAndInvoiceMonthAndInvoiceYear(User student, int invoiceMonth, int invoiceYear);
}
