package com.villavredestein.repository;

import com.villavredestein.model.Huisregel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HuisregelRepository extends JpaRepository<Huisregel, Long> {
    List<Huisregel> findAllByOrderByOrderIndexAscIdAsc();
}
