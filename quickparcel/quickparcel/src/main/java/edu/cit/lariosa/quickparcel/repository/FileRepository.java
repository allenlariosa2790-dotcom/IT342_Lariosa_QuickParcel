package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    @Query("SELECT f FROM File f WHERE f.parcel.id = :parcelId")
    List<File> findByParcelId(@Param("parcelId") Long parcelId);
}