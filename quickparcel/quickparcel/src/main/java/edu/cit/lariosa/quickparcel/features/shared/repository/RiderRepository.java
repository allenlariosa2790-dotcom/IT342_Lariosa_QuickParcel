package edu.cit.lariosa.quickparcel.features.shared.repository;

import edu.cit.lariosa.quickparcel.features.shared.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderRepository extends JpaRepository<Rider, Long> {
}