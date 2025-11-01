package com.villavredestein.repository;

import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudent(User student);
    List<Payment> findByStatus(String status);
    List<Payment> findByStudentEmail(String email);
}