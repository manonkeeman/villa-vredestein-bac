package com.villavredestein.repository;

import com.villavredestein.model.SupplyReport;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplyReportRepository extends JpaRepository<SupplyReport, Long> {
    List<SupplyReport> findByReportedByOrderByReportedAtDesc(User reportedBy);
    List<SupplyReport> findAllByOrderByReportedAtDesc();
}
