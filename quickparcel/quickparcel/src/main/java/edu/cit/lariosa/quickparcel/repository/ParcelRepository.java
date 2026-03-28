package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    List<Parcel> findBySenderId(Long senderId);
}