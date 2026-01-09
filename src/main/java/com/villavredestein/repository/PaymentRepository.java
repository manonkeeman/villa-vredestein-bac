package com.villavredestein.repository;

import com.villavredestein.model.Payment;
import com.villavredestein.model.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStudent_Email(String email);

    List<Payment> findByStudent_EmailOrderByIdDesc(String email);

    List<Payment> findByStudent_EmailAndStatus(String email, PaymentStatus status);

    List<Payment> findByStudent_EmailAndStatusOrderByIdDesc(String email, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusOrderByIdDesc(PaymentStatus status);

    List<Payment> findAllByOrderByIdDesc();
}