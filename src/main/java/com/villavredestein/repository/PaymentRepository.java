package com.villavredestein.repository;

import com.villavredestein.model.Payment;
import com.villavredestein.model.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByIdDesc();

    List<Payment> findByStudent_EmailIgnoreCaseOrderByIdDesc(String email);

    List<Payment> findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(String email, PaymentStatus status);

    List<Payment> findByStatusOrderByIdDesc(PaymentStatus status);
}