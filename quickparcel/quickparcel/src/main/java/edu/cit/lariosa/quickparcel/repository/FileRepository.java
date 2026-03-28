package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByParcelId(Long parcelId);
}