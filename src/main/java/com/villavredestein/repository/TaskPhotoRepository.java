package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.TaskPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskPhotoRepository extends JpaRepository<TaskPhoto, Long> {
    List<TaskPhoto> findByTaskOrderByUploadedAtDesc(CleaningTask task);
}
