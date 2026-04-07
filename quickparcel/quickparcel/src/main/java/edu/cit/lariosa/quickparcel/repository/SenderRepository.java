package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Sender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SenderRepository extends JpaRepository<Sender, Long> {
    @Query("SELECT s FROM Sender s WHERE s.user.id = :userId")
    Optional<Sender> findByUserId(@Param("userId") Long userId);
}