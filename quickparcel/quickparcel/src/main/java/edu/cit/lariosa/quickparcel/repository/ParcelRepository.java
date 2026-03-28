package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    @Query("SELECT p FROM Parcel p WHERE p.sender.userId = :senderId")
    List<Parcel> findBySenderId(@Param("senderId") Long senderId);
}