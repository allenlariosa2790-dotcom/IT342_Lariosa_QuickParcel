package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Sender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenderRepository extends JpaRepository<Sender, Long> {
}